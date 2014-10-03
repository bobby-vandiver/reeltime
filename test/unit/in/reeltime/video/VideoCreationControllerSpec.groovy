package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.TranscoderException
import in.reeltime.exceptions.ProbeException
import in.reeltime.message.LocalizedMessageService
import in.reeltime.user.User
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(VideoCreationController)
@Mock([Video])
class VideoCreationControllerSpec extends AbstractControllerSpec {

    User currentUser

    VideoService videoService
    VideoCreationService videoCreationService

    LocalizedMessageService localizedMessageService

    void setup() {
        currentUser = new User(username: 'bob')
        controller.springSecurityService = Stub(SpringSecurityService) {
            getCurrentUser() >> currentUser
        }

        videoService = Mock(VideoService)
        videoCreationService = Mock(VideoCreationService)
        localizedMessageService = Mock(LocalizedMessageService)

        controller.videoService = videoService
        controller.videoCreationService = videoCreationService
        controller.localizedMessageService = localizedMessageService
    }

    void "return 202 and video id after video has been uploaded with minimum params"() {
        given:
        def videoData = 'foo'.bytes
        def videoParam = new GrailsMockMultipartFile('video', videoData)
        request.addFile(videoParam)

        def title = 'some title'
        params.title = title

        and:
        def validateCommand = { VideoCreationCommand command ->
            assert command.creator == currentUser
            assert command.title == title
            assert command.videoStream.bytes == videoData
            return new Video(title: title).save(validate: false)
        }

        def allowCommand = { VideoCreationCommand command ->
            validateCommand(command)
            command.videoStream = new ByteArrayInputStream(videoData)
            return true
        }

        when:
        controller.upload()

        then:
        1 * videoCreationService.allowCreation(_) >> { command -> allowCommand(command) }
        1 * videoCreationService.createVideo(_) >> { command -> validateCommand(command) }

        and:
        assertStatusCodeAndContentType(response, 202)

        and:
        def json = new JsonSlurper().parseText(response.contentAsString)
        json.size() == 2

        and:
        json.videoId > 0
        json.title == title
    }

    @Unroll
    void "[#exceptionClass] is thrown"() {
        given:
        def message = 'TEST'
        def cause = new Exception('Broke it')

        when:
        controller.upload()

        then:
        assertErrorMessageResponse(response, 503, message)

        and:
        1 * videoCreationService.allowCreation(_) >> true
        1 * videoCreationService.createVideo(_) >> { throw exceptionClass.newInstance(cause) }

        and:
        1 * localizedMessageService.getMessage(messageCode, request.locale) >> message

        where:
        exceptionClass          |   messageCode
        TranscoderException     |   'videoCreation.transcoder.error'
        ProbeException          |   'videoCreation.probe.error'
    }

    @Unroll
    void "status [#statusCode] for video that exists [#exists], current user is creator [#isCreator] and video is available [#available]"() {
        given:
        params.videoId = 1234

        when:
        controller.status()

        then:
        response.status == statusCode
        response.contentLength == 0

        and:
        videoService.videoExists(1234) >> exists
        videoService.currentUserIsVideoCreator(1234) >> isCreator
        videoService.videoIsAvailable(1234) >> available

        where:
        statusCode  |   exists  |   isCreator   |   available
        404         |   false   |   null        |   null
        403         |   true    |   false       |   null
        202         |   true    |   true        |   false
        201         |   true    |   true        |   true
    }
}
