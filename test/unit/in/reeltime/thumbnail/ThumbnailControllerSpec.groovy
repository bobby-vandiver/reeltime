package in.reeltime.thumbnail

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.ThumbnailNotFoundException

@TestFor(ThumbnailController)
class ThumbnailControllerSpec extends AbstractControllerSpec {

    ThumbnailService thumbnailService
    ThumbnailStorageService thumbnailStorageService

    void setup() {
        thumbnailService = Mock(ThumbnailService)
        controller.thumbnailService = thumbnailService

        thumbnailStorageService = Mock(ThumbnailStorageService)
        controller.thumbnailStorageService = thumbnailStorageService
    }

    void "return thumbnail at request resolution if it exists"() {
        given:
        def thumbnail = new Thumbnail(uri: 'somewhere')

        and:
        params.video_id = 1234
        params.resolution = 'small'

        when:
        controller.getThumbnail()

        then:
        response.status == 200
        response.contentType == 'image/png'
        response.contentAsString == 'small thumbnail'

        and:
        1 * thumbnailService.loadThumbnail(1234, ThumbnailResolution.RESOLUTION_1X) >> thumbnail
        1 * thumbnailStorageService.load('somewhere') >> new ByteArrayInputStream('small thumbnail'.bytes)
    }

    void "thumbnail not found"() {
        given:
        params.video_id = 1234
        params.resolution = 'small'

        when:
        controller.getThumbnail()

        then:
        response.status == 404

        and:
        1 * thumbnailService.loadThumbnail(_, _) >> { throw new ThumbnailNotFoundException('TEST') }
    }
}
