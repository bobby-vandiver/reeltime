package in.reeltime.account

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.RegistrationException
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus

@TestFor(ClientManagementController)
class ClientManagementControllerSpec extends AbstractControllerSpec {

    AccountRegistrationService accountRegistrationService
    AccountManagementService accountManagementService
    AuthenticationService authenticationService

    User user

    void setup() {
        accountRegistrationService = Mock(AccountRegistrationService)
        accountManagementService = Mock(AccountManagementService)
        authenticationService = Mock(AuthenticationService)

        controller.accountRegistrationService = accountRegistrationService
        controller.accountManagementService = accountManagementService
        controller.authenticationService = authenticationService

        user = new User(username: 'someone', displayName: 'someone display')
    }

    void "respond with client credentials upon successful registration of a previously unknown client"() {
        given:
        def username = 'foo'
        def password = 'secret'
        def clientName = 'something'

        and:
        def clientId = 'buzz'
        def clientSecret = 'bazz'

        and:
        def registrationResult = new RegistrationResult(clientId: clientId, clientSecret: clientSecret)

        and:
        stubAuthenticationService(true)

        and:
        params.username = username
        params.password = password
        params.client_name = clientName

        when:
        controller.registerClient()

        then:
        assertStatusCodeAndContentType(response, 201)

        and:
        def json = getJsonResponse(response) as Map
        json.size() == 2

        and:
        json.client_id == clientId
        json.client_secret == clientSecret

        and:
        1 * accountRegistrationService.registerClientForExistingUser(username, clientName) >> registrationResult
    }

    void "registration exception is thrown"() {
        given:
        params.username = 'user'
        params.password = 'password'
        params.client_name = 'client'

        and:
        stubAuthenticationService(true)

        when:
        controller.registerClient()

        then:
        assertErrorMessageResponse(response, 503, TEST_MESSAGE)

        and:
        1 * accountRegistrationService.registerClientForExistingUser('user', 'client') >> { throw new RegistrationException('TEST') }
        1 * localizedMessageService.getMessage('registration.internal.error', request.locale) >> TEST_MESSAGE
    }

    void "revoke client"() {
        given:
        def clientId = 'client'
        params.client_id = clientId

        when:
        controller.revokeClient()

        then:
        assertStatusCode(response, 200)

        and:
        1 * authenticationService.getCurrentUser() >> user
        1 * accountManagementService.revokeClient(user, clientId)
    }

    private void stubAuthenticationService(boolean authenticated) {
        defineBeans {
            authenticationService(AuthenticationService)
        }
        def authenticationService = grailsApplication.mainContext.getBean('authenticationService')

        authenticationService.authenticationManager = Stub(AuthenticationManager) {
            authenticate(_) >> {
                if(!authenticated) {
                    throw new BadCredentialsException('TEST')
                }
            }
        }

        // Workaround for GRAILS-10538 per comment by Aaron Long:
        // Source: https://jira.grails.org/browse/GRAILS-10538
        authenticationService.transactionManager = Mock(PlatformTransactionManager) {
            getTransaction(_) >> Mock(TransactionStatus)
        }
    }
}
