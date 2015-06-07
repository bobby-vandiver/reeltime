package in.reeltime.account

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.oauth2.Client
import in.reeltime.exceptions.ClientNotFoundException
import test.helper.UserFactory

class AccountManagementServiceIntegrationSpec extends IntegrationSpec {

    def accountManagementService
    def authenticationService

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
        !authenticationService.authenticateUser(USERNAME, PASSWORD)

        and:
        authenticationService.authenticateUser(USERNAME, newPassword)
    }

    void "change display name"() {
        given:
        def newDisplayName = 'Batman'

        when:
        accountManagementService.changeDisplayName(user, newDisplayName)

        then:
        user.displayName == newDisplayName
    }

    void "verify user"() {
        when:
        accountManagementService.verifyUser(user)

        then:
        user.verified
    }

    void "revoke client for user"() {
        given:
        def clientId = user.clients[0].clientId

        when:
        accountManagementService.revokeClient(user, clientId)

        then:
        user.clients.size() == 0

        and:
        Client.findByClientId(clientId) == null
    }

    void "attempt to revoke client that does not belong to user"() {
        when:
        accountManagementService.revokeClient(user, 'uhoh')

        then:
        def e = thrown(ClientNotFoundException)
        e.message == "Cannot revoke unknown client"
    }
}
