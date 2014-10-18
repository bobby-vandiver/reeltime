package in.reeltime.account

import in.reeltime.common.AbstractControllerIntegrationSpec
import in.reeltime.user.User
import spock.lang.Unroll
import test.helper.UserFactory

class DevelopmentOnlyAccountControllerIntegrationSpec extends AbstractControllerIntegrationSpec {

    DevelopmentOnlyAccountController controller
    User user

    def developmentOnlyAccountService

    def accountCodeGenerationService
    def authenticationService

    void setup() {
        controller = new DevelopmentOnlyAccountController()
        controller.developmentOnlyAccountService = developmentOnlyAccountService

        user = UserFactory.createUser('devOnly')
    }

    void "confirm user account"() {
        given:
        accountCodeGenerationService.generateAccountConfirmationCode(user)

        and:
        controller.params.username = user.username

        when:
        controller.confirmAccountForUser()

        then:
        assertStatusCodeOnlyResponse(controller.response, 200)

        and:
        assertConfirmationCodeHasBeenRemoved(user)
    }

    void "reset password for user"() {
        given:
        accountCodeGenerationService.generateResetPasswordCode(user)

        and:
        controller.params.username = user.username
        controller.params.new_password = 'secret'

        when:
        controller.resetPasswordForUser()

        then:
        assertStatusCodeOnlyResponse(controller.response, 200)

        and:
        assertResetCodeHasBeenRemoved(user)

        and:
        authenticationService.authenticateUser(user.username, 'secret')
    }

    @Unroll
    void "unknown user for action [#actionName]"() {
        given:
        controller.params.username = user.username + 'a'

        when:
        controller."$actionName"()

        then:
        assertStatusCodeOnlyResponse(controller.response, 404)

        where:
        _   |   actionName
        _   |   'confirmAccountForUser'
        _   |   'resetPasswordForUser'
    }
}
