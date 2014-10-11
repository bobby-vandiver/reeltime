package in.reeltime.video

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec

@TestFor(VideoController)
@Mock([Video])
class VideoControllerSpec extends AbstractControllerSpec {

    VideoService videoService

    def setup() {
        videoService = Mock(VideoService)
        controller.videoService = videoService
    }

    void "use page 1 if page param is omitted"() {
        when:
        controller.listVideos()

        then:
        1 * videoService.listVideos(1) >> []
    }

    void "list videos"() {
        given:
        params.page = 3

        and:
        def video = new Video(title: 'buzz').save(validate: false)

        when:
        controller.listVideos()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 1

        and:
        json[0].videoId == video.id
        json[0].title == 'buzz'

        and:
        1 * videoService.listVideos(3) >> [video]
    }
}
