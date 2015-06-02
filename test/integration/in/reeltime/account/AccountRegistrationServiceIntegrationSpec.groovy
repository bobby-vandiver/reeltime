package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.transaction.Transactional
import in.reeltime.user.User
import in.reeltime.user.UserFollowing
import org.springframework.transaction.annotation.Propagation
import test.helper.UserFactory
import test.spec.MailServiceDependentIntegrationSpec

class AccountRegistrationServiceIntegrationSpec extends MailServiceDependentIntegrationSpec {

    def accountRegistrationService
    def accountRemovalService

    @Transactional(propagation = Propagation.NEVER)
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
        SpringSecurityUtils.doWithAuth(username) {
            accountRemovalService.removeAccountForCurrentUser()
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
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

        cleanup:
        SpringSecurityUtils.doWithAuth(username) {
            accountRemovalService.removeAccountForCurrentUser()
        }
    }
}
