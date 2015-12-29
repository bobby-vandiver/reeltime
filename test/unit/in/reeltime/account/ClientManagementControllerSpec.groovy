package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.RegistrationException
import in.reeltime.oauth2.Client
import in.reeltime.oauth2.ClientService
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import in.reeltime.user.UserService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus

@TestFor(ClientManagementController)
@Mock([Client])
class ClientManagementControllerSpec extends AbstractControllerSpec {

    AccountRegistrationService accountRegistrationService
    AccountManagementService accountManagementService
    AuthenticationService authenticationService
    ClientService clientService

    User user
    User currentUser

    void setup() {
        accountRegistrationService = Mock(AccountRegistrationService)
        accountManagementService = Mock(AccountManagementService)
        authenticationService = Mock(AuthenticationService)
        clientService = Mock(ClientService)

        controller.accountRegistrationService = accountRegistrationService
        controller.accountManagementService = accountManagementService
        controller.authenticationService = authenticationService
        controller.clientService = clientService

        user = new User(username: 'someone', displayName: 'someone display')
        currentUser = new User(username: 'current')

        stubUserService()
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

    void "use page 1 if page param is omitted"() {
        when:
        controller.listClients()

        then:
        1 * authenticationService.getCurrentUser() >> currentUser
        1 * clientService.listClientsForUser(currentUser, 1) >> []
    }

    void "list clients"() {
        given:
        params.page = 3

        and:
        def client = new Client(clientId: 'cid', clientName: 'cname')
        client.springSecurityService = Stub(SpringSecurityService)
        client.save(validate: false)

        when:
        controller.listClients()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.clients.size() == 1

        and:
        json.clients[0].client_id == client.clientId
        json.clients[0].client_name == client.clientName

        and:
        1 * authenticationService.getCurrentUser() >> currentUser
        1 * clientService.listClientsForUser(currentUser, 3) >> [client]
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

        authenticationService.userAuthenticationManager = Stub(AuthenticationManager) {
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

    private void stubUserService() {
        defineBeans {
            userService(UserService)
        }

        def userService = grailsApplication.mainContext.getBean('userService')
        userService.metaClass.isClientNameAvailable = { String username, String clientName ->
            return true
        }
    }
}
