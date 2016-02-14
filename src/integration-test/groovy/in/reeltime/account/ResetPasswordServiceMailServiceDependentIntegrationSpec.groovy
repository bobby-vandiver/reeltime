package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.transaction.Rollback
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionDefinition
import in.reeltime.test.factory.UserFactory
import in.reeltime.test.spec.MailServiceDependentIntegrationSpec

@Rollback
class ResetPasswordServiceMailServiceDependentIntegrationSpec extends MailServiceDependentIntegrationSpec {

    @Autowired
    ResetPasswordService resetPasswordService

    @Autowired
    AccountRemovalService accountRemovalService

    User user

    private static final String USERNAME = 'management'
    private static final String DISPLAY_NAME = 'Management Tester'
    private static final String PASSWORD = 'superSecret'
    private static final String EMAIL = 'management@test.com'


    void "send reset password email for verified account"() {
        given:
        def status1 = startTransaction(propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW)

        and:
        createUser(verified: true)

        when:
        resetPasswordService.sendResetPasswordEmail(user, Locale.ENGLISH)

        and:
        commitTransaction(status1)

        then:
        inMemoryMailService.sentMessages.size() == 1

        and:
        def message = inMemoryMailService.sentMessages[0]
        message.subject == 'ReelTime Password Reset'
        message.to == EMAIL
        message.from == 'noreply@reeltime.in'

        and:
        def messageRegex = /Hello (\w+), please enter the following code when prompted to reset your password: ([a-zA-z0-9]{43})/

        def matcher = (message.body =~ messageRegex)
        matcher.matches()

        and:
        matcher[0][1] == user.username
        def sentCode = matcher[0][2] as String

        def confirmationCode = AccountCode.findByUser(user)
        confirmationCode.isCodeCorrect(sentCode)

        cleanup:
        def status2 = startTransaction(propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW)
        removeUser()
        commitTransaction(status2)
    }

    private void createUser(Map overrides = [:]) {
        user = UserFactory.createUser(USERNAME, PASSWORD, DISPLAY_NAME, EMAIL)

        overrides.each { key, value ->
            user."$key" = value
        }
        user.save()
    }

    private void removeUser() {
        SpringSecurityUtils.doWithAuth(USERNAME) {
            accountRemovalService.removeAccountForCurrentUser()
        }
    }
}
