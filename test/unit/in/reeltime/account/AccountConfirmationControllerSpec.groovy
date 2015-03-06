package in.reeltime.account

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.AccountCodeException
import in.reeltime.exceptions.ConfirmationException
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User

@TestFor(AccountConfirmationController)
class AccountConfirmationControllerSpec extends AbstractControllerSpec {

    AccountConfirmationService accountConfirmationService
    AuthenticationService authenticationService

    User user

    void setup() {
        accountConfirmationService = Mock(AccountConfirmationService)
        authenticationService = Mock(AuthenticationService)

        controller.accountConfirmationService = accountConfirmationService
        controller.authenticationService = authenticationService

        user = new User(username: 'current')
    }

    void "send confirmation email"() {
        when:
        controller.sendEmail()

        then:
        assertStatusCode(response, 200)

        and:
        1 * authenticationService.getCurrentUser() >> user
        1 * accountConfirmationService.sendConfirmationEmail(user, request.locale)
    }

    void "account code exception is thrown when sending confirmation email"() {
        when:
        controller.sendEmail()

        then:
        assertErrorMessageResponse(response, 503, TEST_MESSAGE)

        and:
        1 * authenticationService.getCurrentUser() >> user
        1 * accountConfirmationService.sendConfirmationEmail(user, request.locale) >> { throw new AccountCodeException('TEST') }
        1 * localizedMessageService.getMessage('accountConfirmationEmail.internal.error', request.locale) >> TEST_MESSAGE
    }

    void "pass confirmation code to service to complete account confirmation"() {
        given:
        params.code = 'let-me-in'

        when:
        controller.confirmAccount()

        then:
        assertStatusCode(response, 200)

        and:
        1 * accountConfirmationService.confirmAccount('let-me-in')
    }

    void "handle confirmation error"() {
        given:
        params.code = 'uh-oh'

        when:
        controller.confirmAccount()

        then:
        assertStatusCode(response, 403)

        and:
        1 * accountConfirmationService.confirmAccount(_) >> { throw new ConfirmationException('TEST') }
    }
}
