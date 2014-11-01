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
        params.code = 'uh-oh'

        when:
        controller.confirmAccount()

        then:
        assertStatusCodeOnlyResponse(response, 403)

        and:
        1 * accountConfirmationService.confirmAccount(_) >> { throw new ConfirmationException('TEST') }
    }
}
