package in.reeltime.account

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import in.reeltime.user.UserService
import spock.lang.Unroll

@TestFor(ResetPasswordController)
class ResetPasswordControllerSpec extends AbstractControllerSpec {

    ResetPasswordService resetPasswordService
    UserService userService

    User user

    void setup() {
        resetPasswordService = Mock(ResetPasswordService)
        userService = Mock(UserService)

        controller.resetPasswordService = resetPasswordService
        controller.userService = userService

        user = new User(username: 'someone', displayName: 'someone display')
    }

    void "send reset password email for user"() {
        given:
        params.username = user.username

        when:
        controller.sendEmail()

        then:
        assertStatusCodeOnlyResponse(response, 200)

        and:
        1 * userService.loadUser(user.username) >> user
        1 * resetPasswordService.sendResetPasswordEmail(user, request.locale)
    }

    @Unroll
    void "username is required for sending reset password email -- cannot be [#username] "() {
        given:
        params.username = username

        when:
        controller.sendEmail()

        then:
        assertErrorMessageResponse(response, 400, TEST_MESSAGE)

        and:
        1 * localizedMessageService.getMessage('account.reset.password.email.username.required', request.locale) >> TEST_MESSAGE
        0 * resetPasswordService.sendResetPasswordEmail(_, _)

        where:
        _   |   username
        _   |   null
        _   |   ''
    }
}
