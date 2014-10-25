package in.reeltime.account

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import spock.lang.Unroll

@TestFor(AccountManagementController)
class AccountManagementControllerSpec extends AbstractControllerSpec {

    AccountManagementService accountManagementService
    AuthenticationService authenticationService

    User user

    void setup() {
        accountManagementService = Mock(AccountManagementService)
        authenticationService = Mock(AuthenticationService)

        controller.accountManagementService = accountManagementService
        controller.authenticationService = authenticationService

        user = new User(username: 'someone', displayName: 'someone display')
    }

    void "change password"() {
        given:
        def newPassword = 'newSecret'
        params.new_password = newPassword

        when:
        controller.changePassword()

        then:
        assertStatusCodeOnlyResponse(response, 200)

        and:
        1 * authenticationService.getCurrentUser() >> user
        1 * accountManagementService.changePassword(user, newPassword)
    }

    void "change display name"() {
        given:
        def newDisplayName = 'newDisplay'
        params.new_display_name = newDisplayName

        when:
        controller.changeDisplayName()

        then:
        assertStatusCodeOnlyResponse(response, 200)

        and:
        1 * authenticationService.getCurrentUser() >> user
        1 * accountManagementService.changeDisplayName(user, newDisplayName)
    }
}
