package in.reeltime.registration

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.exceptions.RegistrationException
import in.reeltime.user.User
import org.springframework.context.MessageSource
import spock.lang.Specification

@TestFor(RegistrationController)
@Mock([User])
class RegistrationControllerSpec extends Specification {

    UserRegistrationService userRegistrationService
    RegistrationService registrationService

    MessageSource messageSource

    void setup() {
        userRegistrationService = Mock(UserRegistrationService)
        registrationService = Mock(RegistrationService)
        messageSource = Mock(MessageSource)

        controller.userRegistrationService = userRegistrationService
        controller.registrationService = registrationService
        controller.messageSource = messageSource
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
        def registrationResult = new RegistrationResult(clientId: clientId, clientSecret: clientSecret)

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
        1 * userRegistrationService.userExists(_) >> false
        1 * registrationService.registerUserAndClient(username, password, clientName) >> registrationResult
    }

    void "registration exception is thrown"() {
        given:
        def message = 'this is a test'

        when:
        controller.register()

        then:
        response.status == 503
        response.contentType.startsWith('application/json')

        and:
        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        json.size() == 1

        and:
        json.error == message

        and:
        1 * userRegistrationService.userExists(_) >> false
        1 * registrationService.registerUserAndClient(*_) >> { throw new RegistrationException('TEST') }
        1 * messageSource.getMessage('registration.internal.error', [], request.locale) >> message
    }

    void "user already exists"() {
        given:
        def username = 'foo'
        params.username = username

        and:
        def message = 'TEST'

        when:
        controller.register()

        then:
        response.status == 400
        response.contentType.startsWith('application/json')

        and:
        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        json.size() == 1

        and:
        json.error == message

        and:
        1 * userRegistrationService.userExists(username) >> true
        1 * messageSource.getMessage('registration.user.exists', [username] as Object[], request.locale) >> message
    }
}
