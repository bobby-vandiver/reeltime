package in.reeltime.registration

import grails.plugins.rest.client.RestResponse
import helper.oauth2.AccessTokenRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class RegistrationFunctionalSpec extends FunctionalSpec {

    @Override
    protected String getResource() {
        return '/register'
    }

    @Unroll
    void "invalid http method [#method]"() {
        when:
        def response = "$method"() as RestResponse

        then:
        response.status == 405
        response.body == ''

        where:
        _   |   method
        _   |   'get'
        _   |   'put'
        _   |   'delete'
    }

    void "register a new user"() {
        when:
        def response = post() {
            username = 'newUser'
            password = 'newPassword'
            client_name = 'newClient'
        }

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
                password: 'newPassword',
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

        when:
        def response = post() {
            username = name
            password = 'password'
            client_name = 'client'
        }

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors[0] == '[username] is not available'
    }

    @Unroll
    void "invalid params username [#user], password [#pass], client_name [#client]"() {
        when:
        def response = post() {
            username = user
            password = pass
            client_name = client
        }

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors[0] == message

        where:
        user     | pass     | client     | message
        'user'   | 'pass'   | ''         | '[client_name] is required'
        'user'   | 'pass'   | null       | '[client_name] is required'

        ''       | 'pass'   | 'client'   | '[username] is required'
        null     | 'pass'   | 'client'   | '[username] is required'

        'user'   | ''       | 'client'   | '[password] is required'
        'user'   | null     | 'client'   | '[password] is required'
    }

    void "missing all params"() {
        when:
        def response = post()

        then:
        response.status == 400
        response.json.errors.size() == 3

        and:
        response.json.errors.contains('[client_name] is required')
        response.json.errors.contains('[username] is required')
        response.json.errors.contains('[password] is required')
    }

    private void registerUser(String name) {
        def response = post() {
            username = name
            password = 'password'
            client_name = 'client'
        }
        assert response.status == 201
    }
}
