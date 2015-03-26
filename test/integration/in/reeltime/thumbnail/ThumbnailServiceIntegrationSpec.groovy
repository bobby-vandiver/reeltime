package in.reeltime.thumbnail

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.video.Video
import test.helper.UserFactory
import test.helper.VideoFactory
import in.reeltime.exceptions.ThumbnailNotFoundException
import static in.reeltime.thumbnail.ThumbnailResolution.*

class ThumbnailServiceIntegrationSpec extends IntegrationSpec {

    def thumbnailService
    def thumbnailStorageService

    User user
    Video video

    void setup() {
        user = UserFactory.createUser('thumbnail')
        video = VideoFactory.createVideo(user, 'some video')
    }

    void "add thumbnails to video"() {
        given:
        storeSourceImageForVideo()

        when:
        thumbnailService.addThumbnails(video)

        then:
        video.thumbnails.size() == 3

        and:
        video.thumbnails.find { it.resolution == RESOLUTION_1X } != null
        video.thumbnails.find { it.resolution == RESOLUTION_2X } != null
        video.thumbnails.find { it.resolution == RESOLUTION_3X } != null
    }

    void "load thumbnail by resolution and video id"() {
        given:
        video.addToThumbnails(uri: 'something', resolution: RESOLUTION_2X)
        video.save()

        when:
        def thumbnail = thumbnailService.loadThumbnail(video.id, RESOLUTION_2X)

        then:
        thumbnail.uri == 'something'
        thumbnail.resolution == RESOLUTION_2X
    }

    void "attempt to load thumbnail that doesn't exist"() {
        when:
        thumbnailService.loadThumbnail(video.id, RESOLUTION_3X)

        then:
        def e = thrown(ThumbnailNotFoundException)
        e.message == "Thumbnail for video [${video.id}] and resolution [RESOLUTION_3X] not found"
    }

    private void storeSourceImageForVideo() {
        def imagePath = 'test/files/images/small.png'
        def imageStream = new FileInputStream(imagePath)

        def path = thumbnailStorageService.uniqueThumbnailPath
        thumbnailStorageService.store(imageStream, path)

        video.masterThumbnailPath = path
        video.save(failOnError: true)
    }
}
