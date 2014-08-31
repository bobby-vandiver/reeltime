package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.reel.Reel
import in.reeltime.user.User
import test.helper.UserFactory

class VideoCreationServiceIntegrationSpec extends IntegrationSpec {

    def videoCreationService

    User creator
    Reel reel

    void setup() {
        creator = UserFactory.createTestUser()
        reel = creator.reels[0]
    }

    void "create a valid video"() {
        given:
        def reelName = reel.name
        def title = 'fun times'
        def videoStream = new File('test/files/small.mp4').newInputStream()

        and:
        def command = new VideoCreationCommand(creator: creator, title: title, reel: reelName, videoStream: videoStream)

        when:
        SpringSecurityUtils.doWithAuth(creator.username) {
            videoCreationService.createVideo(command)
        }

        then:
        def video = Video.findByCreator(creator)

        video.creator == creator
        video.title == title
        video.masterPath != null

        and:
        video.reels.size() == 1
        video.reels.contains(reel)
        reel.videos.contains(video)
    }
}
