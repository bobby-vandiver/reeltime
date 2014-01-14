package in.reeltime.video

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import in.reeltime.storage.PathGenerationService
import in.reeltime.user.User
import in.reeltime.storage.InputStorageService
import in.reeltime.transcoder.TranscoderService

@TestFor(VideoService)
@Mock([Video])
class VideoServiceSpec extends Specification {

    void setup() {
        service.inputStorageService = Mock(InputStorageService)
        service.pathGenerationService = Mock(PathGenerationService)
        service.transcoderService = Mock(TranscoderService)
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
}
