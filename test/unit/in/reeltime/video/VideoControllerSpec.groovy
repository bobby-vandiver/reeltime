package in.reeltime.video

import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification

import in.reeltime.user.UserAuthenticationService

@TestFor(VideoController)
class VideoControllerSpec extends Specification {

    void "return 401 if attempting to upload video without being logged in"() {
        given:
        mockUserAuthenticationService(false)

        when:
        controller.upload()

        then:
        response.status == 401
    }

    void "return 400 if video param is missing from request"() {
        given:
        mockUserAuthenticationService(true)

        when:
        controller.upload()

        then:
        response.status == 400
    }

    void "return 400 if title param is missing from request"() {
        given:
        mockUserAuthenticationService(true)

        def videoParam = new GrailsMockMultipartFile('video', 'foo'.bytes)
        request.addFile(videoParam)

        when:
        controller.upload()

        then:
        response.contentType.contains('application/json')
        response.status == 400

        and:
        def json = new JsonSlurper().parseText(response.contentAsString)
        json.message == 'Title is required'
    }

    void "return 201 after video has been uploaded with minimum params"() {
        given:
        mockUserAuthenticationService(true)

        def videoParam = new GrailsMockMultipartFile('video', 'foo'.bytes)
        request.addFile(videoParam)

        def title = 'some title'
        params.title = title

        and:
        controller.videoSubmissionService = Mock(VideoSubmissionService)

        def validateArgs = { Video video, InputStream input ->
            assert video.title == title
            assert input.bytes == videoParam.inputStream.bytes
        }

        when:
        controller.upload()

        then:
        1 * controller.videoSubmissionService.submit(*_) >> { args -> validateArgs(args) }

        and:
        response.status == 201
    }

    private void mockUserAuthenticationService(boolean isLoggedIn) {
        controller.userAuthenticationService = Mock(UserAuthenticationService) {
            1 * isUserLoggedIn() >> isLoggedIn
        }
    }
}
