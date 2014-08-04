package in.reeltime.account

import helper.oauth2.AccessTokenRequest
import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class RegistrationFunctionalSpec extends FunctionalSpec {

    static String token

    void setupSpec() {
        def username = 'registerTest'
        def result = registerUser(username)

        def accessTokenRequest = new AccessTokenRequest(
                username: username,
                password: TEST_PASSWORD,
                grantType: 'password',
                clientId: result.client_id,
                clientSecret: result.client_secret,
                scope: ['view']
        )
        token = getAccessTokenWithScope(accessTokenRequest)
    }

    @Unroll
    void "invalid http methods for register endpoint"() {
        expect:
        assertInvalidHttpMethods(registerUrl, ['get', 'put', 'delete'])
    }

    void "register a new user"() {
        given:
        def request = createRegisterRequest {
            email = 'someone@somewhere.com'
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
        def request = createRegisterRequest {
            email = 'email@test.com'
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
    void "invalid params email [#emailAddress], username [#user], password [#pass], client_name [#client]"() {
        given:
        def request = createRegisterRequest {
            email = emailAddress
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
        emailAddress        |   user     | pass     | client     | message
        ''                  |   'user'   | 'secret' | 'client'   | '[email] is required'
        null                |   'user'   | 'secret' | 'client'   | '[email] is required'
        'test@'             |   'user'   | 'secret' | 'client'   | '[email] is not a valid e-mail address'

        'test@reeltime.in'  |   'user'   | 'secret' | ''         | '[client_name] is required'
        'test@reeltime.in'  |   'user'   | 'secret' | null       | '[client_name] is required'

        'test@reeltime.in'  |   ''       | 'secret' | 'client'   | '[username] is required'
        'test@reeltime.in'  |   null     | 'secret' | 'client'   | '[username] is required'
        'test@reeltime.in'  |   'a'      | 'secret' | 'client'   | '[username] must be 2-15 alphanumeric characters long'
        'test@reeltime.in'  |   '1234a!' | 'secret' | 'client'   | '[username] must be 2-15 alphanumeric characters long'

        'test@reeltime.in'  |   'user'   | ''       | 'client'   | '[password] is required'
        'test@reeltime.in'  |   'user'   | null     | 'client'   | '[password] is required'
        'test@reeltime.in'  |   'user'   | 'short'  | 'client'   | '[password] must be at least 6 characters long'
    }

    void "missing all params"() {
        given:
        def request = createRegisterRequest()

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 4

        and:
        response.json.errors.contains('[client_name] is required')
        response.json.errors.contains('[username] is required')
        response.json.errors.contains('[password] is required')
        response.json.errors.contains('[email] is required')
    }

    void "invalid http methods for verify endpoint"() {
        expect:
        assertInvalidHttpMethods(verifyUrl, ['get', 'put', 'delete'], token)
    }

    private static getRegisterUrl() {
        getUrlForResource('account/register')
    }

    private static getVerifyUrl() {
        getUrlForResource("account/confirm")
    }

    private static RestRequest createRegisterRequest(Closure params = null) {
        new RestRequest(url: registerUrl, customizer: params)
    }
}
