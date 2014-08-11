package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.message.LocalizedMessageService
import spock.lang.Unroll

@TestFor(ReelController)
class ReelControllerSpec extends AbstractControllerSpec {

    ReelService reelService
    LocalizedMessageService localizedMessageService

    void setup() {
        reelService = Mock(ReelService)
        localizedMessageService = Mock(LocalizedMessageService)

        controller.reelService = reelService
        controller.localizedMessageService = localizedMessageService
    }

    void "successfully add a new reel"() {
        given:
        def reelName = 'test-reel-name'
        params.name = reelName

        when:
        controller.addReel()

        then:
        response.status == 201
        response.contentLength == 0
    }

    void "unable to add reel with invalid name"() {
        given:
        def reelName = 'invalid-reel'
        params.name = reelName

        and:
        def message = 'reel bad name'

        when:
        controller.addReel()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * reelService.addReel(reelName) >> { throw new InvalidReelNameException('TEST') }
        1 * localizedMessageService.getMessage('reel.invalid.name', request.locale) >> message
    }

    @Unroll
    void "reel name must be present cannot be [#name]"() {
        given:
        def message = 'reel name required'

        and:
        params.name = name

        when:
        controller.addReel()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * localizedMessageService.getMessage('reel.name.required', request.locale) >> message

        where:
        _   |   name
        _   |   null
        _   |   ''
    }
}
