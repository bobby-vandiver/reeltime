package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.reel.ReelVideo
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.file.FileLoader
import in.reeltime.test.factory.UserFactory

@Integration
@Rollback
class VideoCreationServiceIntegrationSpec extends Specification {

    @Autowired
    VideoCreationService videoCreationService

    void "create a valid video"() {
        given:
        def creator = UserFactory.createTestUser()
        def reel = creator.reels[0]

        def reelName = reel.name
        def title = 'fun times'

        def videoStream = FileLoader.videoFile('small.mp4').newInputStream()
        def thumbnailStream = FileLoader.imageFile('small.png').newInputStream()

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
