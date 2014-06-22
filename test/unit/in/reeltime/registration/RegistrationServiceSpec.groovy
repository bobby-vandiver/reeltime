package in.reeltime.registration

import grails.test.mixin.TestFor
import in.reeltime.oauth2.Client
import spock.lang.Specification

@TestFor(RegistrationService)
class RegistrationServiceSpec extends Specification {

    UserRegistrationService userRegistrationService
    ClientRegistrationService clientRegistrationService

    void setup() {
        userRegistrationService = Mock(UserRegistrationService)
        clientRegistrationService = Mock(ClientRegistrationService)

        service.userRegistrationService = userRegistrationService
        service.clientRegistrationService = clientRegistrationService
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
        1 * clientRegistrationService.generateClientId() >> clientId
        1 * clientRegistrationService.generateClientSecret() >> clientSecret
        1 * clientRegistrationService.register(clientName, clientId, clientSecret) >> client

        and:
        1 * userRegistrationService.register(username, password, client)
    }
}
