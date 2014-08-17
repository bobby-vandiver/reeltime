package in.reeltime.account

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User

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
        def clientName = 'something'

        and:
        def command = new AccountRegistrationCommand(username: username, password: password,
                email: email, client_name: clientName)

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
        def password = 'bar'

        def firstClientName = 'first one'
        def secondClientName = 'second one'

        and:
        registerNewUser(username, password, firstClientName)

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

    private RegistrationResult registerNewUser(String username, String password, String clientName) {
        def command = new AccountRegistrationCommand(username: username, password: password,
                email: "$username@test.com", client_name: clientName)

        accountRegistrationService.registerUserAndClient(command, Locale.ENGLISH)
    }
}
