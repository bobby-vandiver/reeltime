package in.reeltime.thumbnail

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.exceptions.ThumbnailNotFoundException
import in.reeltime.user.User
import in.reeltime.video.Video
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory
import in.reeltime.test.factory.VideoFactory

import static in.reeltime.thumbnail.ThumbnailResolution.*

@Integration
@Rollback
class ThumbnailServiceIntegrationSpec extends Specification {

    @Autowired
    ThumbnailService thumbnailService

    @Autowired
    ThumbnailStorageService thumbnailStorageService

    User user
    Video video

    void "add thumbnails to video"() {
        given:
        setupData()

        and:
        storeSourceImageForVideo()

        when:
        thumbnailService.addThumbnails(video)

        then:
        video.thumbnails.size() == 3

        and:
        video.thumbnails.find { it.resolution == RESOLUTION_1X } != null
        video.thumbnails.find { it.resolution == RESOLUTION_2X } != null
        video.thumbnails.find { it.resolution == RESOLUTION_3X } != null

        and:
        ThumbnailVideo.findAllByVideo(video).find { it.thumbnail.resolution == RESOLUTION_1X } != null
        ThumbnailVideo.findAllByVideo(video).find { it.thumbnail.resolution == RESOLUTION_2X } != null
        ThumbnailVideo.findAllByVideo(video).find { it.thumbnail.resolution == RESOLUTION_3X } != null
    }

    void "load thumbnail by resolution and video id"() {
        given:
        setupData()

        and:
        new ThumbnailVideo(
                thumbnail: new Thumbnail(uri: 'something', resolution: RESOLUTION_2X).save(),
                video: video
        ).save()

        when:
        def thumbnail = thumbnailService.loadThumbnail(video.id, RESOLUTION_2X)

        then:
        thumbnail.uri == 'something'
        thumbnail.resolution == RESOLUTION_2X
    }

    void "attempt to load thumbnail that doesn't exist"() {
        given:
        setupData()

        when:
        thumbnailService.loadThumbnail(video.id, RESOLUTION_3X)

        then:
        def e = thrown(ThumbnailNotFoundException)
        e.message == "Thumbnail for video [${video.id}] and resolution [RESOLUTION_3X] not found"
    }

    private void setupData() {
        user = UserFactory.createUser('thumbnail')
        video = VideoFactory.createVideo(user, 'some video')
    }

    private void storeSourceImageForVideo() {
        def imagePath = 'src/test/resources/files/images/small.png'
        def imageStream = new FileInputStream(imagePath)

        def path = thumbnailStorageService.uniqueThumbnailPath
        thumbnailStorageService.store(imageStream, path)

        video.masterThumbnailPath = path
        video.save(failOnError: true)
    }
}
