package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.oauth2.Client
import in.reeltime.reel.AudienceMember
import in.reeltime.reel.AudienceService
import in.reeltime.reel.UserReel
import in.reeltime.user.User
import in.reeltime.user.UserFollowing
import in.reeltime.user.UserFollowingService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory

@Integration
@Rollback
class AccountRemovalServiceIntegrationSpec extends Specification {

    @Autowired
    AccountRemovalService accountRemovalService

    @Autowired
    AccountRegistrationService accountRegistrationService

    @Autowired
    AudienceService audienceService

    @Autowired
    UserFollowingService userFollowingService

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

        and:
        def query = {
            or {
                eq("follower", user)
                eq("followee", user)
            }
        }

        assert UserFollowing.withCriteria(query).size() == 2

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
        UserFollowing.createCriteria().get(query) == null

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
