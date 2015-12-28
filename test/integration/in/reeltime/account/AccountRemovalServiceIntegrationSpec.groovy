package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.oauth2.Client
import in.reeltime.user.UserFollowing
import test.helper.UserFactory
import in.reeltime.reel.AudienceMember
import in.reeltime.reel.UserReel

class AccountRemovalServiceIntegrationSpec extends IntegrationSpec {

    def accountRemovalService
    def accountRegistrationService

    def audienceService
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

        and:
        def reelOwner = UserFactory.createUser('ownsReel')
        def reelId = reelOwner.reels[0].id

        SpringSecurityUtils.doWithAuth(username) {
            audienceService.addCurrentUserToAudience(reelId)
        }

        assert AudienceMember.findAllByMember(user).size() == 1

        when:
        SpringSecurityUtils.doWithAuth(username) {
            accountRemovalService.removeAccountForCurrentUser()
        }

        then:
        User.findByUsername(username) == null
        AccountCode.findByUser(user) == null

        and:
        UserFollowing.findByFollowerOrFollowee(user, user) == null

        and:
        AudienceMember.findAllByMember(user).size() == 0
        UserReel.findAllByOwner(user).size() == 0

        and:
        Client.findByClientNameAndClientId(firstClientName, firstClientId) == null
        Client.findByClientNameAndClientId(secondClientName, secondClientId) == null
    }

    private RegistrationResult registerNewUser(String username, String password, String clientName) {
        def command = new AccountRegistrationCommand(
                username: username,
                password: password,
                display_name: username,
                email: "$username@test.com",
                client_name: clientName
        )
        accountRegistrationService.registerUserAndClient(command, Locale.ENGLISH)
    }
}
