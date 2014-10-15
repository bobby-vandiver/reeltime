package in.reeltime.account

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import test.helper.UserFactory

class AccountManagementServiceIntegrationSpec extends IntegrationSpec {

    def accountManagementService
    def userAuthenticationService

    User user

    private static final String USERNAME = 'management'
    private static final String DISPLAY_NAME = 'Management Tester'
    private static final String PASSWORD = 'superSecret'
    private static final String EMAIL = 'management@test.com'

    void setup() {
        user = UserFactory.createUser(USERNAME, PASSWORD, DISPLAY_NAME, EMAIL)
    }

    void "change password"() {
        given:
        def newPassword = 'newSecret'

        when:
        accountManagementService.changePassword(user, newPassword)

        then:
        !userAuthenticationService.authenticate(USERNAME, PASSWORD)

        and:
        userAuthenticationService.authenticate(USERNAME, newPassword)
    }

    void "change display name"() {
        given:
        def newDisplayName = 'Batman'

        when:
        accountManagementService.changeDisplayName(user, newDisplayName)

        then:
        user.displayName == newDisplayName
    }
}
