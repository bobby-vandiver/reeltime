package in.reeltime.account

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.AccountCodeException
import in.reeltime.exceptions.RegistrationException
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import in.reeltime.user.UserService
import in.reeltime.user.UsernameCommand

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
        def command = new UsernameCommand(username: user.username)

        when:
        controller.sendEmail(command)

        then:
        assertStatusCode(response, 200)

        and:
        1 * userService.loadUser(user.username) >> user
        1 * resetPasswordService.sendResetPasswordEmail(user, request.locale)
    }

    void "account code exception is thrown when sending reset password email"() {
        given:
        def command = new UsernameCommand(username: user.username)

        when:
        controller.sendEmail(command)

        then:
        assertErrorMessageResponse(response, 503, TEST_MESSAGE)

        and:
        1 * userService.loadUser(user.username) >> user
        1 * resetPasswordService.sendResetPasswordEmail(user, request.locale) >> { throw new AccountCodeException('TEST') }
        1 * localizedMessageService.getMessage('account.password.reset.email.internal.error', request.locale) >> TEST_MESSAGE
    }

    void "registration exception is thrown when resetting password and registering new client"() {
        given:
        def command = new ResetPasswordCommand(
                username: 'foo',
                new_password: 'secret',
                code: 'reset',
                client_is_registered: false,
                client_name: 'something',
                authenticationService: Stub(AuthenticationService),
                userService: Stub(UserService)
        )

        when:
        controller.resetPassword(command)

        then:
        assertErrorMessageResponse(response, 503, TEST_MESSAGE)

        and:
        1 * resetPasswordService.resetPasswordForNewClient(_, _, _, _) >> { throw new RegistrationException('TEST') }
        1 * localizedMessageService.getMessage('registration.internal.error', request.locale) >> TEST_MESSAGE
    }
}
