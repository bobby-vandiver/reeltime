package in.reeltime.registration

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.exceptions.RegistrationException
import in.reeltime.user.User
import org.springframework.context.MessageSource
import spock.lang.Specification
import in.reeltime.user.UserService

@TestFor(RegistrationController)
@Mock([User])
class RegistrationControllerSpec extends Specification {

    RegistrationService registrationService
    MessageSource messageSource

    void setup() {
        registrationService = Mock(RegistrationService)
        messageSource = Mock(MessageSource)

        controller.registrationService = registrationService
        controller.messageSource = messageSource

        defineBeans {
            userService(UserService)
        }
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
        1 * registrationService.registerUserAndClient(username, password, clientName) >> registrationResult
    }

    void "registration exception is thrown"() {
        given:
        params.username = 'foo'
        params.password = 'bar'
        params.client_name = 'something'

        and:
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
        1 * registrationService.registerUserAndClient(*_) >> { throw new RegistrationException('TEST') }
        1 * messageSource.getMessage('registration.internal.error', [], request.locale) >> message
    }
}
