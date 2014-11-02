package in.reeltime.video

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.TranscoderException
import in.reeltime.exceptions.ProbeException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Unroll

@TestFor(VideoController)
@Mock([Video])
class VideoControllerSpec extends AbstractControllerSpec {

    User currentUser

    VideoService videoService
    VideoCreationService videoCreationService
    VideoRemovalService videoRemovalService

    void setup() {
        currentUser = new User(username: 'bob')
        controller.authenticationService = Stub(AuthenticationService) {
            getCurrentUser() >> currentUser
        }

        videoService = Mock(VideoService)
        videoCreationService = Mock(VideoCreationService)
        videoRemovalService = Mock(VideoRemovalService)

        controller.videoService = videoService
        controller.videoCreationService = videoCreationService
        controller.videoRemovalService = videoRemovalService
    }

    void "use page 1 if page param is omitted"() {
        when:
        controller.listVideos()

        then:
        1 * videoService.listVideos(1) >> []
    }

    void "list videos"() {
        given:
        params.page = 3

        and:
        def video = new Video(title: 'buzz').save(validate: false)

        when:
        controller.listVideos()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 1

        and:
        json[0].video_id == video.id
        json[0].title == 'buzz'

        and:
        1 * videoService.listVideos(3) >> [video]
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
        json.video_id > 0
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
        params.video_id = 1234

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

    void "pass videoId to service for removal"() {
        given:
        params.video_id = 1234

        when:
        controller.remove()

        then:
        response.status == 200

        and:
        1 * videoRemovalService.removeVideoById(1234)
    }

    void "attempt to remove video that does not exist"() {
        given:
        def message = 'TEST'

        and:
        params.video_id = 1234

        when:
        controller.remove()

        then:
        assertErrorMessageResponse(response, 404, message)

        and:
        1 * videoRemovalService.removeVideoById(1234) >> { throw new VideoNotFoundException('') }
        1 * localizedMessageService.getMessage('video.unknown', request.locale) >> message
    }
}
