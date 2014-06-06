package in.reeltime.video

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

    void setup() {
        streamMetadataService = Mock(StreamMetadataService) {
            extractStreams(_) >> StreamMetadataListFactory.createRequiredStreams()
        }
        service.inputStorageService = Mock(InputStorageService)
        service.pathGenerationService = Mock(PathGenerationService)
        service.transcoderService = Mock(TranscoderService)
        service.streamMetadataService = streamMetadataService

        grailsApplication.config.reeltime.metadata.maxDurationInSeconds = '300'
        grailsApplication.config.reeltime.metadata.maxVideoStreamSizeInBytes = '1000'
    }

    void "store video stream, save the video object and then transcode it"() {
        given:
        def creator = new User(username: 'bob')
        def title = 'fun times'
        def videoStream = new ByteArrayInputStream('yay'.bytes)

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
        def video = service.createVideo(creator, title, videoStream)

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

    @Unroll
    void "video stream with data [#data] and max allowed size [#max] is allowed [#allowed]"() {
        given:
        grailsApplication.config.reeltime.metadata.maxVideoStreamSizeInBytes = max

        and:
        def stream = new ByteArrayInputStream(data)
        def command = new VideoCreationCommand(videoStream: stream)

        expect:
        service.allowCreation(command) == allowed

        where:
        max     |   data        |   allowed
        '0'     |   'a'.bytes   |   false
        '1'     |   'a'.bytes   |   true
        '1'     |   'ab'.bytes  |   false
        '2'     |   'a'.bytes   |   true
    }

    void "video stream is larger than buffer but less than max size allowed"() {
        given:
        grailsApplication.config.reeltime.metadata.maxVideoStreamSizeInBytes = "${4 * service.BUFFER_SIZE}"

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

    private static VideoCreationCommand createCommandWithVideoStream(byte[] data) {
        def videoStream = new ByteArrayInputStream(data)
        new VideoCreationCommand(videoStream: videoStream)
    }
}
