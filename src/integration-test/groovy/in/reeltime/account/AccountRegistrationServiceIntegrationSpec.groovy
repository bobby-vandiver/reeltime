package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory

@Integration
@Rollback
class AccountRegistrationServiceIntegrationSpec extends Specification {

    @Autowired
    AccountRegistrationService accountRegistrationService

    @Autowired
    AccountRemovalService accountRemovalService

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
