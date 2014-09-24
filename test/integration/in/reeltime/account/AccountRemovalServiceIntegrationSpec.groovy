package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.oauth2.Client
import in.reeltime.user.UserFollowing
import test.helper.UserFactory

class AccountRemovalServiceIntegrationSpec extends IntegrationSpec {

    def accountRemovalService
    def accountRegistrationService

    def userFollowingService

    void "remove account for current user"() {
        given:
        def username = 'foo'
        def password = 'bar'

        def firstClientName = 'first one'
        def secondClientName = 'second one'

        and:
        def firstClientId = registerNewUser(username, password, firstClientName).clientId
        def secondClientId = accountRegistrationService.registerClientForExistingUser(username, secondClientName).clientId

        and:
        def user = User.findByUsername(username)
        assert user != null

        and:
        def userToFollow = UserFactory.createUser('someone')
        def userToBeFollowedBy = UserFactory.createUser('anyone')

        userFollowingService.startFollowingUser(user, userToFollow)
        userFollowingService.startFollowingUser(userToBeFollowedBy, user)

        assert UserFollowing.findAllByFollowerOrFollowee(user, user).size() == 2

        when:
        SpringSecurityUtils.doWithAuth(username) {
            accountRemovalService.removeAccountForCurrentUser()
        }

        then:
        User.findByUsername(username) == null
        UserFollowing.findByFollowerOrFollowee(user, user) == null
        AccountConfirmation.findByUser(user) == null

        and:
        Client.findByClientNameAndClientId(firstClientName, firstClientId) == null
        Client.findByClientNameAndClientId(secondClientName, secondClientId) == null
    }

    private RegistrationResult registerNewUser(String username, String password, String clientName) {
        def command = new AccountRegistrationCommand(username: username, password: password,
                email: "$username@test.com", client_name: clientName)

        accountRegistrationService.registerUserAndClient(command, Locale.ENGLISH)
    }
}
