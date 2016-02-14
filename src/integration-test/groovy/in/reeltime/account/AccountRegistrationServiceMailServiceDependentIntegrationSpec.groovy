package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.transaction.Rollback
import in.reeltime.user.User
import in.reeltime.user.UserFollowing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionDefinition
import in.reeltime.test.spec.MailServiceDependentIntegrationSpec

@Rollback
class AccountRegistrationServiceMailServiceDependentIntegrationSpec extends MailServiceDependentIntegrationSpec {

    @Autowired
    AccountRegistrationService accountRegistrationService

    @Autowired
    AccountRemovalService accountRemovalService

    void "return client id and client secret in registration result and send confirmation email"() {
        given:
        def status1 = startTransaction(propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW)

        and:
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

        and:
        commitTransaction(status1)

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
        UserFollowing.withCriteria {
            or {
                eq('follower', user)
                eq('followee', user)
            }
        }.size() == 0

        and:
        inMemoryMailService.sentMessages.size() == 1

        and:
        def message = inMemoryMailService.sentMessages[0]
        message.subject == 'Please Verify Your ReelTime Account'
        message.to == email
        message.from == 'noreply@reeltime.in'

        and:
        def messageRegex = /Hello (\w+), please enter the following code on your registered device: ([a-zA-z0-9]{43})/

        def matcher = (message.body =~ messageRegex)
        matcher.matches()

        and:
        matcher[0][1] == username
        def sentCode = matcher[0][2] as String

        def confirmationCode = AccountCode.findByUser(user)
        confirmationCode.isCodeCorrect(sentCode)

        cleanup:
        def status2 = startTransaction(propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW)

        SpringSecurityUtils.doWithAuth(username) {
            accountRemovalService.removeAccountForCurrentUser()
        }

        commitTransaction(status2)
    }
}
