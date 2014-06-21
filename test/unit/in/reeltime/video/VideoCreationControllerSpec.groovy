package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.user.User
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification

@TestFor(VideoCreationController)
@Mock([Video])
class VideoCreationControllerSpec extends Specification {

    User currentUser
    VideoCreationService videoCreationService

    void setup() {
        currentUser = new User(username: 'bob')
        controller.springSecurityService = Stub(SpringSecurityService) {
            getCurrentUser() >> currentUser
        }

        videoCreationService = Mock(VideoCreationService)
        controller.videoCreationService = videoCreationService
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
            return new Video().save(validate: false)
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
        response.status == 202
        response.contentType.startsWith('application/json')

        and:
        def json = new JsonSlurper().parseText(response.contentAsString)
        json.videoId > 0
    }
}
