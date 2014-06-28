package in.reeltime.registration

import grails.plugins.rest.client.RestResponse
import helper.oauth2.AccessTokenRequest
import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class RegistrationFunctionalSpec extends FunctionalSpec {

    @Unroll
    void "invalid http methods"() {
        expect:
        assertInvalidHttpMethods(url, ['get', 'put', 'delete'])
    }

    void "register a new user"() {
        given:
        def request = createRequest {
            username = 'newUser'
            password = 'n3wP4s$w0rd!'
            client_name = 'newClient'
        }

        when:
        def response = post(request)

        then:
        response.status == 201
        response.json.client_id
        response.json.client_secret

        and:
        def clientId = response.json.client_id
        def clientSecret = response.json.client_secret

        and:
        def tokenRequest = new AccessTokenRequest(
                clientId: clientId,
                clientSecret: clientSecret,
                username: 'newUser',
                password: 'n3wP4s$w0rd!',
                grantType: 'password',
                scope: ['view']
        )

        when:
        def token = getAccessTokenWithScope(tokenRequest)

        then:
        token != null
    }

    void "username is not available"() {
        given:
        def name = 'existingUser'
        registerUser(name)

        and:
        def request = createRequest {
            username = name
            password = 'password'
            client_name = 'client'
        }

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors[0] == '[username] is not available'
    }

    @Unroll
    void "invalid params username [#user], password [#pass], client_name [#client]"() {
        given:
        def request = createRequest {
            username = user
            password = pass
            client_name = client
        }

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors[0] == message

        where:
        user     | pass     | client     | message
        'user'   | 'secret' | ''         | '[client_name] is required'
        'user'   | 'secret' | null       | '[client_name] is required'

        ''       | 'secret' | 'client'   | '[username] is required'
        null     | 'secret' | 'client'   | '[username] is required'

        'a'      | 'secret' | 'client'   | '[username] must be 2-15 alphanumeric characters long'
        '1234a!' | 'secret' | 'client'   | '[username] must be 2-15 alphanumeric characters long'

        'user'   | ''       | 'client'   | '[password] is required'
        'user'   | null     | 'client'   | '[password] is required'

        'user'   | 'short'  | 'client'   | '[password] must be at least 6 characters long'
    }

    void "missing all params"() {
        given:
        def request = createRequest()

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 3

        and:
        response.json.errors.contains('[client_name] is required')
        response.json.errors.contains('[username] is required')
        response.json.errors.contains('[password] is required')
    }

    private static getUrl() {
        getUrlForResource('register')
    }

    private static RestRequest createRequest(Closure params = null) {
        new RestRequest(url: url, customizer: params)
    }
}
