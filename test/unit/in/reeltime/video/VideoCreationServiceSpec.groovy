package in.reeltime.video

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.metadata.StreamMetadata
import in.reeltime.metadata.StreamMetadataService
import in.reeltime.playlist.PlaylistService
import in.reeltime.reel.Reel
import in.reeltime.reel.ReelVideoManagementService
import in.reeltime.playlist.PlaylistAndSegmentStorageService
import in.reeltime.storage.TemporaryFileService
import in.reeltime.thumbnail.ThumbnailValidationResult
import in.reeltime.transcoder.TranscoderJob
import in.reeltime.transcoder.TranscoderJobService
import in.reeltime.transcoder.TranscoderService
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll
import test.helper.StreamMetadataListFactory
import in.reeltime.thumbnail.ThumbnailService
import in.reeltime.thumbnail.ThumbnailStorageService
import in.reeltime.thumbnail.ThumbnailValidationService

@TestFor(VideoCreationService)
@Mock([Video, TranscoderJob, User, VideoCreator])
class VideoCreationServiceSpec extends Specification {

    StreamMetadataService streamMetadataService
    TemporaryFileService temporaryFileService

    private static final MAX_DURATION = 300
    private static final MAX_VIDEO_STREAM_SIZE = 1000
    private static final MAX_THUMBNAIL_STREAM_SIZE = 1000

    void setup() {
        streamMetadataService = Mock(StreamMetadataService) {
            extractStreams(_) >> StreamMetadataListFactory.createRequiredStreams()
        }

        service.videoService = Mock(VideoService)
        service.videoStorageService = Mock(VideoStorageService)

        service.playlistAndSegmentStorageService = Mock(PlaylistAndSegmentStorageService)
        service.playlistService = Mock(PlaylistService)

        service.transcoderService = Mock(TranscoderService)
        service.transcoderJobService = Mock(TranscoderJobService)

        service.streamMetadataService = streamMetadataService

        service.reelVideoManagementService = Mock(ReelVideoManagementService)

        service.thumbnailService = Mock(ThumbnailService)
        service.thumbnailStorageService = Mock(ThumbnailStorageService)

        service.thumbnailValidationService = Mock(ThumbnailValidationService) {
            validateThumbnailStream(_) >> new ThumbnailValidationResult(validFormat: true)
        }

        // TODO: Use a mock and refactor stream tests
        temporaryFileService = new TemporaryFileService()
        service.temporaryFileService = temporaryFileService

        VideoCreationCommand.maxDuration = MAX_DURATION
        service.maxVideoStreamSizeInBytes = MAX_VIDEO_STREAM_SIZE
        service.maxThumbnailStreamSizeInBytes = MAX_THUMBNAIL_STREAM_SIZE
    }

    void "store video stream, save the video object and then transcode it"() {
        given:
        def reelName = 'something'
        def reel = new Reel(name: reelName)
        def creator = new User(username: 'bob', reels: [reel])
        def title = 'fun times'
        def videoStream = new ByteArrayInputStream('yay'.bytes)
        def thumbnailStream = new ByteArrayInputStream('woo'.bytes)

        and:
        def command = new VideoCreationCommand(creator: creator, title: title, reel: reelName,
                videoStream: videoStream, thumbnailStream: thumbnailStream)

        and:
        def masterPath = 'foo'
        def playlistPath = 'bar'
        def masterThumbnailPath = 'buzz'

        and:
        def validateVideoArg = { Video v ->
            assert v.title == title
            assert v.masterPath == masterPath
            assert v.masterThumbnailPath == masterThumbnailPath
        }

        and:
        def validateReelArg = { Reel r ->
            assert r.name == reelName
        }

        when:
        def video = service.createVideo(command)

        then:
        1 * service.videoStorageService.getUniqueVideoPath() >> masterPath
        1 * service.videoStorageService.store(videoStream, masterPath)

        and:
        1 * service.playlistAndSegmentStorageService.getUniquePlaylistPath() >> playlistPath
        1 * service.transcoderService.transcode(_ as Video, playlistPath) >> { args -> validateVideoArg(args[0])}

        and:
        1 * service.thumbnailStorageService.getUniqueThumbnailPath() >> masterThumbnailPath
        1 * service.thumbnailStorageService.store(thumbnailStream, masterThumbnailPath)

        and:
        1 * service.reelVideoManagementService.addVideoToReel(_ as Reel, _ as Video) >> { args ->
            def r = args[0] as Reel
            def v = args[1] as Video

            validateReelArg(r)
            validateVideoArg(v)
        }

        and:
        1 * service.videoService.storeVideo(_) >> { Video v -> validateVideoArg(v) }

        and:
        video.creator == creator
        video.title == title
        video.masterPath == masterPath
    }

    void "video stream is required"() {
        given:
        def command = new VideoCreationCommand(videoStream: null)

        expect:
        !service.allowCreation(command)
    }

    void "video stream size is valid flag should be null if video stream is not available"() {
        given:
        def command = new VideoCreationCommand(videoStream: null)

        when:
        service.allowCreation(command)

        then:
        command.videoStreamSizeIsValid == null
    }

    @Unroll
    void "video stream with data [#data] and max allowed size [#max] is allowed [#allowed]"() {
        given:
        service.maxVideoStreamSizeInBytes = max

        and:
        def command = createCommandWithVideoStream(data)

        expect:
        service.allowCreation(command) == allowed

        where:
        max     |   data        |   allowed
        0       |   'a'.bytes   |   false
        1       |   'a'.bytes   |   true
        1       |   'ab'.bytes  |   false
        2       |   'a'.bytes   |   true
    }

    void "video stream is larger than buffer but less than max size allowed"() {
        given:
        service.maxVideoStreamSizeInBytes = 4 * temporaryFileService.BUFFER_SIZE

        and:
        def data = 'a' * (3 * temporaryFileService.BUFFER_SIZE)
        def command = createCommandWithVideoStream(data.bytes)

        expect:
        service.allowCreation(command)
    }

    void "reload the video stream after writing it to the temp file"() {
        given:
        def data = 'TEST'.bytes
        def command = createCommandWithVideoStream(data)

        when:
        service.allowCreation(command)

        then:
        command.videoStream.bytes == data
    }

    void "extract max duration of streams in video"() {
        given:
        def streams = [
                new StreamMetadata(duration: '1234.000000'),
                new StreamMetadata(duration: '678.000000'),
                new StreamMetadata(duration: '9000.999999'),
                new StreamMetadata(duration: '421.000000'),
                new StreamMetadata(duration: '9000.00000')
        ]

        and:
        def command = createCommandWithVideoStream('TEST'.bytes)

        when:
        service.allowCreation(command)

        then:
        command.durationInSeconds == 9001

        and:
        1 * streamMetadataService.extractStreams(_) >> streams
    }

    void "duration should be null if no streams were found"() {
        given:
        def command = createCommandWithVideoStream('TEST'.bytes)

        when:
        service.allowCreation(command)

        then:
        command.durationInSeconds == null

        and:
        1 * streamMetadataService.extractStreams(_) >> []
    }

    @Unroll
    void "invalid when stream for required codec [#codec] is not present"() {
        given:
        def streams = StreamMetadataListFactory.createRequiredStreams()
        def streamToRemove = streams.find { it.codecName == codec }
        streams.remove(streamToRemove)

        and:
        def command = createCommandWithVideoStream('TEST'.bytes)

        when:
        def allowed = service.allowCreation(command)

        then:
        !allowed

        and:
        1 * streamMetadataService.extractStreams(_) >> streams

        where:
        _   |   codec
        _   |   'h264'
        _   |   'aac'
    }

    void "invalid thumbnail stream -- skip thumbnail dependent validation"() {
        given:
        def command = createCommandWithVideoStream('VIDEO'.bytes)
        command.thumbnailStream = null

        expect:
        !service.allowCreation(command)

        and:
        command.thumbnailFormatIsValid.is(null)
        command.thumbnailStreamSizeIsValid.is(null)
    }

    void "invalid thumbnail format"() {
        given:
        def command = createCommandWithVideoStream('VIDEO'.bytes)
        def validationResult = new ThumbnailValidationResult(validFormat: false)

        when:
        def allowed = service.allowCreation(command)

        then:
        !allowed

        and:
        !command.thumbnailFormatIsValid.is(null)
        !command.thumbnailFormatIsValid

        and:
        1 * service.thumbnailValidationService.validateThumbnailStream(_) >> validationResult
    }

    @Unroll
    void "thumbnail stream with data [#data] and max allowed size [#max] is allowed [#allowed]"() {
        given:
        service.maxThumbnailStreamSizeInBytes = max

        and:
        def command = createCommandWithVideoStream('VIDEO'.bytes)
        command.thumbnailStream = new ByteArrayInputStream(data)

        expect:
        service.allowCreation(command) == allowed

        and:
        command.thumbnailStreamSizeIsValid == allowed

        where:
        max     |   data        |   allowed
        0       |   'a'.bytes   |   false
        1       |   'a'.bytes   |   true
        1       |   'ab'.bytes  |   false
        2       |   'a'.bytes   |   true
    }

    void "add playlists and thumbnails to completed video"() {
        given:
        def video = new Video()
        def transcoderJob = new TranscoderJob(video: video, jobId: '1388444889472-t01s28').save(validate: false)

        when:
        service.completeVideoCreation('1388444889472-t01s28', 'hls-small/', 'hls-small-master')

        then:
        1 * service.transcoderJobService.loadJob('1388444889472-t01s28') >> transcoderJob
        1 * service.transcoderJobService.complete(transcoderJob)
        1 * service.playlistService.addPlaylists(video, 'hls-small/', 'hls-small-master')
        1 * service.thumbnailService.addThumbnails(video)
    }

    private static VideoCreationCommand createCommandWithVideoStream(byte[] data) {
        def reel = new Reel(name: 'test-reel')
        def creator = new User(username: 'videoCreationTestUser', reels: [reel])
        def videoStream = new ByteArrayInputStream(data)
        def thumbnailStream = new ByteArrayInputStream('THUMBNAIL'.bytes)
        new VideoCreationCommand(creator: creator, videoStream: videoStream,
                thumbnailStream: thumbnailStream, title: 'test-title', reel: 'test-reel')
    }
}
