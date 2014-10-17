package in.reeltime.account

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User

@TestFor(ResetPasswordController)
class ResetPasswordControllerSpec extends AbstractControllerSpec {

    ResetPasswordService resetPasswordService
    AuthenticationService authenticationService

    User user

    void setup() {
        resetPasswordService = Mock(ResetPasswordService)
        authenticationService = Mock(AuthenticationService)

        controller.resetPasswordService = resetPasswordService
        controller.authenticationService = authenticationService

        user = new User(username: 'someone', displayName: 'someone display')
    }

    void "send reset password email for current user"() {
        when:
        controller.sendEmail()

        then:
        assertStatusCodeOnlyResponse(response, 200)

        and:
        1 * authenticationService.getCurrentUser() >> user
        1 * resetPasswordService.sendResetPasswordEmail(user, request.locale)
    }
}
