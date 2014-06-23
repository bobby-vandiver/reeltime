package in.reeltime.registration

import grails.test.mixin.TestFor
import in.reeltime.oauth2.Client
import in.reeltime.oauth2.ClientService
import in.reeltime.user.UserService
import spock.lang.Specification

@TestFor(RegistrationService)
class RegistrationServiceSpec extends Specification {

    UserService userService
    ClientService clientService

    void setup() {
        userService = Mock(UserService)
        clientService = Mock(ClientService)

        service.userService = userService
        service.clientService = clientService
    }

    void "return client id and client secret in registration result"() {
        given:
        def username = 'foo'
        def password = 'bar'
        def clientName = 'something'

        and:
        def clientId = 'buzz'
        def clientSecret = 'bazz'

        and:
        def client = new Client()

        when:
        def result = service.registerUserAndClient(username, password, clientName)

        then:
        result.clientId == clientId
        result.clientSecret == clientSecret

        and:
        1 * clientService.generateClientId() >> clientId
        1 * clientService.generateClientSecret() >> clientSecret
        1 * clientService.createClient(clientName, clientId, clientSecret) >> client

        and:
        1 * userService.createUser(username, password, client)
    }
}
