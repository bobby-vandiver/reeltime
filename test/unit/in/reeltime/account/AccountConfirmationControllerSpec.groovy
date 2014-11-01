package in.reeltime.account

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.ConfirmationException
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import spock.lang.Unroll

@TestFor(AccountConfirmationController)
class AccountConfirmationControllerSpec extends AbstractControllerSpec {

    AccountConfirmationService accountConfirmationService
    AuthenticationService authenticationService

    void setup() {
        accountConfirmationService = Mock(AccountConfirmationService)
        authenticationService = Mock(AuthenticationService)

        controller.accountConfirmationService = accountConfirmationService
        controller.authenticationService = authenticationService
    }

    void "send confirmation email"() {
        given:
        def user = new User(username: 'current')

        when:
        controller.sendEmail()

        then:
        assertStatusCodeOnlyResponse(response, 200)

        and:
        1 * authenticationService.getCurrentUser() >> user
        1 * accountConfirmationService.sendConfirmationEmail(user, request.locale)
    }

    void "pass confirmation code to service to complete account confirmation"() {
        given:
        params.code = 'let-me-in'

        when:
        controller.confirmAccount()

        then:
        assertStatusCodeOnlyResponse(response, 200)

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
