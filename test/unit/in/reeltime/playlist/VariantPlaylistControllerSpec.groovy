package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import in.reeltime.video.Video

@TestFor(VariantPlaylistController)
@Mock([Video])
class VariantPlaylistControllerSpec extends Specification {

    void "return a 404 if the video does not exist"() {
        given:
        params.videoId = 1234

        when:
        controller.getVariantPlaylist()

        then:
        response.status == 404
    }

    void "return a 200 and the variant playlist for the requested video"() {
        given:
        def video = new Video(title: 'test').save(validate: false)
        params.videoId = video.id
        assert video.id

        and:
        controller.variantPlaylistService = Mock(VariantPlaylistService)

        when:
        controller.getVariantPlaylist()

        then:
        1 * controller.variantPlaylistService.generateVariantPlaylist(video) >> 'some playlist'

        and:
        response.status == 200
        response.contentType == 'application/x-mpegURL'
    }
}
