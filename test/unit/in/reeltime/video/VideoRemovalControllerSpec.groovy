package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.message.LocalizedMessageService
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(VideoRemovalController)
class VideoRemovalControllerSpec extends AbstractControllerSpec {

    VideoRemovalService videoRemovalService
    LocalizedMessageService localizedMessageService

    void setup() {
        videoRemovalService = Mock(VideoRemovalService)
        localizedMessageService = Mock(LocalizedMessageService)

        controller.videoRemovalService = videoRemovalService
        controller.localizedMessageService = localizedMessageService
    }

    void "pass videoId to service for removal"() {
        given:
        params.videoId = 1234

        when:
        controller.remove()

        then:
        response.status == 200

        and:
        1 * videoRemovalService.removeVideoById(1234)
    }

    @Unroll
    void "videoId cannot be [#videoId]"() {
        given:
        def message = 'TEST'

        and:
        params.videoId = videoId

        when:
        controller.remove()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * localizedMessageService.getMessage('video.id.required', request.locale) >> message

        where:
        _   |   videoId
        _   |   null
        _   |   ''
    }

    void "attempt to remove video that does not exist"() {
        given:
        def message = 'TEST'

        and:
        params.videoId = 1234

        when:
        controller.remove()

        then:
        assertErrorMessageResponse(response, 404, message)

        and:
        1 * videoRemovalService.removeVideoById(1234) >> { throw new VideoNotFoundException('') }
        1 * localizedMessageService.getMessage('video.unknown', request.locale) >> message
    }
}
