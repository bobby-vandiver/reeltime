package in.reeltime.registration

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User

class RegistrationServiceIntegrationSpec extends IntegrationSpec {

    def registrationService
    def inMemoryMailService

    void setup() {
        registrationService.mailService = inMemoryMailService
    }

    void cleanup() {
        inMemoryMailService.deleteAllMessages()
    }

    void "send account confirmation email"() {
        given:
        def username = 'foo'
        def email = 'foo@test.com'

        and:
        def user = registerUserWithUsernameAndEmail(username, email)

        when:
        registrationService.sendConfirmationEmail(username, email, Locale.ENGLISH)

        then:
        AccountConfirmation.findByUser(user)

        and:
        inMemoryMailService.sentMessages.size() == 1

        and:
        def message = inMemoryMailService.sentMessages[0]
        message.subject == 'Please Verify Your ReelTime Account'
        message.to == email
        message.from == 'registration@reeltime.in'
        message.body.startsWith("Hello $username, please enter the following code on your registered device:")
    }

    private User registerUserWithUsernameAndEmail(String username, String email) {
        def command = new RegistrationCommand(username: username, password: 'bar',
                email: email, client_name: 'test-bot')
        registrationService.registerUserAndClient(command)

        def user = User.findByUsername(username)
        assert user != null
        return user
    }
}
