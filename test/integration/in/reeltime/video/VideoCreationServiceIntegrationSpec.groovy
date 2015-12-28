package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.reel.Reel
import in.reeltime.reel.ReelVideo
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

        def videoStream = new File('test/files/videos/small.mp4').newInputStream()
        def thumbnailStream = new File('test/files/images/small.png').newInputStream()

        and:
        def command = new VideoCreationCommand(
                creator: creator,
                title: title,
                reel: reelName,
                videoStream: videoStream,
                thumbnailStream: thumbnailStream
        )

        when:
        SpringSecurityUtils.doWithAuth(creator.username) {
            videoCreationService.createVideo(command)
        }

        then:
        def video = VideoCreator.findByCreator(creator).video

        video.creator == creator
        video.title == title
        video.masterPath != null
        video.masterThumbnailPath != null

        and:
        creator.videos.contains(video)

        and:
        ReelVideo.findByReelAndVideo(reel, video) != null
    }
}
