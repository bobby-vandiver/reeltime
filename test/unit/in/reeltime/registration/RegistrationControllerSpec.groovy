package in.reeltime.registration

import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.exceptions.RegistrationException
import in.reeltime.oauth2.Client
import spock.lang.Specification

@TestFor(RegistrationController)
class RegistrationControllerSpec extends Specification {

    UserRegistrationService userRegistrationService
    ClientRegistrationService clientRegistrationService

    void setup() {
        userRegistrationService = Mock(UserRegistrationService)
        clientRegistrationService = Mock(ClientRegistrationService)

        controller.userRegistrationService = userRegistrationService
        controller.clientRegistrationService = clientRegistrationService
    }

    void "response with client credentials upon successful registration"() {
        given:
        def username = 'foo'
        def password = 'bar'
        def clientName = 'something'

        and:
        def clientId = 'buzz'
        def clientSecret = 'bazz'

        and:
        def client = new Client()

        and:
        params.username = username
        params.password = password
        params.client_name = clientName

        when:
        controller.register()

        then:
        response.status == 201
        response.contentType.startsWith('application/json')

        and:
        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        json.size() == 2

        and:
        json.client_id == clientId
        json.client_secret == clientSecret

        and:
        1 * clientRegistrationService.generateClientId() >> clientId
        1 * clientRegistrationService.generateClientSecret() >> clientSecret
        1 * clientRegistrationService.register(clientName, clientId, clientSecret) >> client

        and:
        1 * userRegistrationService.register(username, password, client)
    }

    void "registration exception is thrown"() {
        when:
        controller.register()

        then:
        response.status == 503
        response.contentType.startsWith('application/json')

        and:
        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        json.size() == 1

        and:
        json.error == 'Unable to register. Please try again.'

        and:
        1 * clientRegistrationService.generateClientId() >> { throw new RegistrationException('TEST') }
    }
}
