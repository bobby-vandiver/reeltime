package in.reeltime.video

import grails.test.spock.IntegrationSpec
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
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

        and:
        controller.params.remove(param)
        controller.request.fileMap.remove(param)

        when:
        controller.upload()

        then:
        controller.response.contentType.contains('application/json')
        controller.response.status == 400

        and:
        def json = new JsonSlurper().parseText(controller.response.contentAsString)
        json.errors.contains(message)

        where:
        param   |   message
        'title' |   '[title] is required'
        'video' |   '[video] is required'
    }

    private void setupForCreationRequest() {
        controller.request.method = 'POST'
        controller.params.put('title', 'test-title')

        def video = new GrailsMockMultipartFile('video', 'test-video'.bytes)
        controller.request.addFile(video)
    }
}
