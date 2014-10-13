package in.reeltime.account

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import test.helper.UserFactory
import in.reeltime.exceptions.AuthorizationException

class AccountManagementServiceIntegrationSpec extends IntegrationSpec {

    def accountManagementService
    def userAuthenticationService
    def inMemoryMailService

    User user

    private static final String USERNAME = 'management'
    private static final String DISPLAY_NAME = 'Management Tester'
    private static final String PASSWORD = 'superSecret'
    private static final String EMAIL = 'management@test.com'

    void setup() {
        user = UserFactory.createUser(USERNAME, PASSWORD, DISPLAY_NAME, EMAIL)
        inMemoryMailService.deleteAllMessages()
    }

    void cleanup() {
        inMemoryMailService.deleteAllMessages()
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

    void "do not allow password reset email to be sent on an account that has not been verified"() {
        given:
        user.verified = false
        user.save()

        when:
        accountManagementService.sendResetPasswordEmail(user, Locale.ENGLISH)

        then:
        def e = thrown(AuthorizationException)
        e.message == "Cannot reset a password if the account has not been verified"

        and:
        userAuthenticationService.authenticate(USERNAME, PASSWORD)
    }

    void "send reset password email for verified account"() {
        given:
        user.verified = true
        user.save()

        when:
        accountManagementService.sendResetPasswordEmail(user, Locale.ENGLISH)

        then:
        inMemoryMailService.sentMessages.size() == 1

        and:
        def message = inMemoryMailService.sentMessages[0]
        message.subject == 'ReelTime Password Reset'
        message.to == EMAIL
        message.from == 'registration@reeltime.in'
    }
}
