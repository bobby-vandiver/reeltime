package in.reeltime.video

import grails.test.mixin.TestFor
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

    void "return 201 after video has been uploaded"() {
        given:
        mockUserAuthenticationService(true)

        def video = new GrailsMockMultipartFile('video', 'foo'.bytes)
        request.addFile(video)

        and:
        boolean submitCalled = false
        controller.videoSubmissionService = [
                submit: { input ->
                    submitCalled = true
                    assert input.bytes == video.inputStream.bytes
                }
        ] as VideoSubmissionService

        when:
        controller.upload()

        then:
        response.status == 201

        and:
        submitCalled
    }

    private void mockUserAuthenticationService(boolean isLoggedIn) {
        controller.userAuthenticationService = Mock(UserAuthenticationService) {
            1 * isUserLoggedIn() >> isLoggedIn
        }
    }
}
