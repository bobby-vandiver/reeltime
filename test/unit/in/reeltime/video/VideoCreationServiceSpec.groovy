package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.metadata.StreamMetadata
import spock.lang.Specification
import in.reeltime.storage.PathGenerationService
import in.reeltime.user.User
import in.reeltime.storage.InputStorageService
import in.reeltime.transcoder.TranscoderService
import in.reeltime.metadata.StreamMetadataService
import spock.lang.Unroll
import test.helper.StreamMetadataListFactory

@TestFor(VideoCreationService)
@Mock([Video])
class VideoCreationServiceSpec extends Specification {

    StreamMetadataService streamMetadataService

    private static final MAX_DURATION = 300
    private static final MAX_VIDEO_STREAM_SIZE = 1000

    void setup() {
        streamMetadataService = Mock(StreamMetadataService) {
            extractStreams(_) >> StreamMetadataListFactory.createRequiredStreams()
        }
        service.inputStorageService = Mock(InputStorageService)
        service.pathGenerationService = Mock(PathGenerationService)
        service.transcoderService = Mock(TranscoderService)
        service.streamMetadataService = streamMetadataService

        VideoCreationCommand.maxDuration = MAX_DURATION
        service.maxVideoStreamSizeInBytes = MAX_VIDEO_STREAM_SIZE
    }

    void "store video stream, save the video object and then transcode it"() {
        given:
        def creator = new User(username: 'bob')
        def title = 'fun times'
        def videoStream = new ByteArrayInputStream('yay'.bytes)

        and:
        def command = new VideoCreationCommand(creator: creator, title: title, videoStream: videoStream)

        and:
        def masterPath = 'foo'
        def outputPath = 'bar'

        and:
        def validateTranscodeVideoArgs = { Video v ->
            assert v.creator == creator
            assert v.title == title
            assert v.masterPath == masterPath
        }

        when:
        def video = service.createVideo(command)

        then:
        1 * service.pathGenerationService.getUniqueInputPath() >> masterPath
        1 * service.inputStorageService.store(videoStream, masterPath)

        and:
        1 * service.pathGenerationService.getUniqueOutputPath() >> outputPath
        1 * service.transcoderService.transcode(_ as Video, outputPath) >> { args -> validateTranscodeVideoArgs(args[0])}

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
        service.maxVideoStreamSizeInBytes = 4 * service.BUFFER_SIZE

        and:
        def data = 'a' * (3 * service.BUFFER_SIZE)
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

    private static VideoCreationCommand createCommandWithVideoStream(byte[] data) {
        def creator = new User(username: 'videoCreationTestUser')
        def videoStream = new ByteArrayInputStream(data)
        new VideoCreationCommand(creator: creator, videoStream: videoStream, title: 'test-title')
    }
}
