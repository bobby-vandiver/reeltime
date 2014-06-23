package in.reeltime.registration

import grails.test.spock.IntegrationSpec
import groovy.json.JsonSlurper
import in.reeltime.user.User
import spock.lang.Unroll

class RegistrationControllerIntegrationSpec extends IntegrationSpec {

    RegistrationController controller

    def registrationService
    def messageSource

    void setup() {
        controller = new RegistrationController()
        controller.registrationService = registrationService
        controller.messageSource = messageSource

        controller.request.method = 'POST'
    }

    void "user already exists"() {
        given:
        def username = 'foo'
        new User(username: username, password: 'bar').save(validate: false)

        controller.params.username = username

        when:
        controller.register()

        then:
        controller.response.status == 400
        controller.response.contentType.startsWith('application/json')

        and:
        def json = new JsonSlurper().parseText(controller.response.contentAsString) as Map
        json.size() == 1

        and:
        json.errors.contains("[username] is not available")
    }

    @Unroll
    void "invalid params username [#username], password [#password], client_name [#clientName]"() {
        given:
        controller.params.username = username
        controller.params.password = password
        controller.params.client_name = clientName

        when:
        controller.register()

        then:
        controller.response.status == 400
        controller.response.contentType.startsWith('application/json')

        and:
        def json = new JsonSlurper().parseText(controller.response.contentAsString) as Map
        json.size() == 1

        and:
        json.errors.contains(message)

        where:
        username    |   password    |   clientName  |   message
        'user'      |   'pass'      |   ''          |   '[client_name] is required'
        'user'      |   'pass'      |   null        |   '[client_name] is required'

        ''          |   'pass'      |   'client'    |   '[username] is required'
        null        |   'pass'      |   'client'    |   '[username] is required'

        'user'      |   ''          |   'client'    |   '[password] is required'
        'user'      |   null        |   'client'    |   '[password] is required'
    }

}
