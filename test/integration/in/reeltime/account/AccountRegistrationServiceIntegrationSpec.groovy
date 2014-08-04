package in.reeltime.account

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User

class AccountRegistrationServiceIntegrationSpec extends IntegrationSpec {

    def accountRegistrationService
    def inMemoryMailService

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
        def command = new RegistrationCommand(username: username, password: password,
                email: email, client_name: clientName)

        when:
        def result = accountRegistrationService.registerUserAndClient(command, Locale.ENGLISH)

        then:
        result.clientId != null
        result.clientSecret != null

        and:
        User.findByUsernameAndEmail(username, email) != null

        and:
        inMemoryMailService.sentMessages.size() == 1

        and:
        def message = inMemoryMailService.sentMessages[0]
        message.subject == 'Please Verify Your ReelTime Account'
        message.to == email
        message.from == 'registration@reeltime.in'
        message.body.startsWith("Hello $username, please enter the following code on your registered device:")
    }
}
