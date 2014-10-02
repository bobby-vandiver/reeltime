package in.reeltime.account

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.user.UserFollowing
import test.helper.UserFactory

class AccountRegistrationServiceIntegrationSpec extends IntegrationSpec {

    def accountRegistrationService
    def inMemoryMailService

    void setup() {
        inMemoryMailService.deleteAllMessages()
    }

    void cleanup() {
        inMemoryMailService.deleteAllMessages()
    }

    void "return client id and client secret in registration result and send confirmation email"() {
        given:
        def email = 'foo@test.com'
        def username = 'foo'
        def password = 'bar'
        def displayName = 'foo bar'
        def clientName = 'something'

        and:
        def command = new AccountRegistrationCommand(username: username, password: password,
                display_name: displayName, email: email, client_name: clientName)

        when:
        def result = accountRegistrationService.registerUserAndClient(command, Locale.ENGLISH)

        then:
        result.clientId != null
        result.clientSecret != null

        and:
        def user = User.findByUsernameAndEmail(username, email)
        user != null

        and:
        user.reels.size() == 1
        user.reels[0].name == 'Uncategorized'

        and:
        UserFollowing.findAllByFollowerOrFollowee(user, user).size() == 0

        and:
        inMemoryMailService.sentMessages.size() == 1

        and:
        def message = inMemoryMailService.sentMessages[0]
        message.subject == 'Please Verify Your ReelTime Account'
        message.to == email
        message.from == 'registration@reeltime.in'
        message.body.startsWith("Hello $username, please enter the following code on your registered device:")
    }

    void "register a new client for an existing user"() {
        given:
        def username = 'foo'
        def newUser = UserFactory.createUser(username)

        def firstClientName = newUser.clients[0].clientName
        def secondClientName = firstClientName + 'a'

        when:
        def result = accountRegistrationService.registerClientForExistingUser(username, secondClientName)

        then:
        result.clientId != null
        result.clientSecret != null

        and:
        def user = User.findByUsername(username)
        user != null

        and:
        user.clients.size() == 2
        user.clients.find { it.clientName == firstClientName } != null
        user.clients.find { it.clientName == secondClientName } != null
    }
}
