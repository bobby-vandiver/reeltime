package in.reeltime.account

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.ConfirmationException
import spock.lang.Unroll

@TestFor(AccountConfirmationController)
class AccountConfirmationControllerSpec extends AbstractControllerSpec {

    AccountConfirmationService accountConfirmationService

    void setup() {
        accountConfirmationService = Mock(AccountConfirmationService)
        controller.accountConfirmationService = accountConfirmationService
    }

    @Unroll
    void "confirmation code must be present -- cannot be [#code]"() {
        given:
        def message = 'confirmation code required'

        and:
        params.code = code

        when:
        controller.confirmAccount()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * localizedMessageService.getMessage('registration.confirmation.code.required', request.locale) >> message

        where:
        _   |   code
        _   |   null
        _   |   ''
    }

    void "pass confirmation code to service to complete account confirmation"() {
        given:
        params.code = 'let-me-in'

        when:
        controller.confirmAccount()

        then:
        response.status == 200
        response.contentLength == 0

        and:
        1 * accountConfirmationService.confirmAccount('let-me-in')
    }

    void "handle confirmation error"() {
        given:
        def message = 'confirmation error'

        and:
        params.code = 'uh-oh'

        when:
        controller.confirmAccount()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * accountConfirmationService.confirmAccount(_) >> { throw new ConfirmationException('TEST') }
        1 * localizedMessageService.getMessage('registration.confirmation.code.error', request.locale) >> message
    }
}
