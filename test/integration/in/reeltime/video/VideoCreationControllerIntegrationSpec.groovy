package in.reeltime.video

import grails.test.spock.IntegrationSpec
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Unroll

class VideoCreationControllerIntegrationSpec extends IntegrationSpec {

    VideoCreationController controller

    def userAuthenticationService
    def videoCreationService

    def messageSource

    void setup() {
        controller = new VideoCreationController()
        controller.userAuthenticationService = userAuthenticationService
        controller.videoCreationService = videoCreationService
        controller.messageSource = messageSource
    }

    @Unroll
    void "param [#param] is missing from request"() {
        given:
        setupForCreationRequest()
        addStubVideoFile()

        and:
        controller.params.remove(param)
        controller.request.fileMap.remove(param)

        when:
        controller.upload()

        then:
        assertErrorResponseContainsMessage(message)

        where:
        param   |   message
        'title' |   '[title] is required'
        'video' |   '[video] is required'
    }

    @Unroll
    void "submit invalid video file [#path]"() {
        given:
        setupForCreationRequest()
        addFileAsVideo(path)

        when:
        controller.upload()

        then:
        assertErrorResponseContainsMessage(message)

        where:
        path                |   message
        'test/files/empty'  |   '[video] does not contain an h264 video and/or an aac audio streams'
    }

    private void setupForCreationRequest() {
        controller.request.method = 'POST'
        controller.params.put('title', 'test-title')
    }

    private void addStubVideoFile() {
        def video = new GrailsMockMultipartFile('video', 'test-video'.bytes)
        controller.request.addFile(video)
    }

    private void addFileAsVideo(String path) {
        def stream = new FileInputStream(path)
        def video = new GrailsMockMultipartFile('video', stream)
        controller.request.addFile(video)
    }

    private void assertErrorResponseContainsMessage(String message) {
        assert controller.response.contentType.contains('application/json')
        assert controller.response.status == 400

        def json = new JsonSlurper().parseText(controller.response.contentAsString)
        assert json.errors.contains(message)
    }
}
