package in.reeltime.registration

import com.icegreen.greenmail.util.GreenMailUtil
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User

class RegistrationServiceIntegrationSpec extends IntegrationSpec {

    def registrationService
    def greenMail

    void cleanup() {
        greenMail.deleteAllMessages()
    }

    void "send account verification email"() {
        given:
        def username = 'foo'
        def email = 'foo@test.com'

        and:
        def user = registerUserWithUsernameAndEmail(username, email)

        when:
        registrationService.sendVerificationEmail(username, email, Locale.ENGLISH)

        then:
        AccountVerification.findByUser(user)

        and:
        greenMail.receivedMessages.size() == 1

        and:
        def message = greenMail.receivedMessages[0]
        message.subject == 'Please Verify Your ReelTime Account'

        and:
        GreenMailUtil.getAddressList(message.allRecipients) == email
        GreenMailUtil.getAddressList(message.from) == RegistrationService.FROM_ADDRESS
        GreenMailUtil.getBody(message).startsWith("Hello $username, please enter the following code on your registered device:")
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
