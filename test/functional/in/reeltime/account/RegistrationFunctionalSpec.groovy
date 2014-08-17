package in.reeltime.account

import helper.oauth2.AccessTokenRequest
import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import junit.framework.Assert
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
                scope: ['account-write']
        )
        token = getAccessTokenWithScope(accessTokenRequest)
    }

    void cleanupSpec() {
        def removeAccountUrl = getUrlForResource('account')
        def request = new RestRequest(url: removeAccountUrl, token: token)

        def response = delete(request)
        if(response.status != 200) {
            Assert.fail("Failed to remove access token")
        }

        response = delete(request)
        if(response.status != 401) {
            Assert.fail("The token associated with the deleted account is still valid! Status: ${response.status}")
        }
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
                scope: [
                        'account-read', 'account-write',
                        'audiences-read', 'audiences-write',
                        'reels-read', 'reels-write',
                        'videos-read', 'videos-write'
                ]
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

    void "register client with bad credentials"() {
        given:
        def request = new RestRequest(url: registerClientUrl, customizer: {
            username = 'user'
            password = 'pass'
            client_name = 'client'
        })

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors[0] == 'Invalid credentials'
    }

    @Unroll
    void "register client with invalid params username [#user], password [#pass], client_name [#client]"() {
        given:
        def request = new RestRequest(url: registerClientUrl, customizer: {
            username = user
            password = pass
            client_name = client
        })

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() > 0
        response.json.errors.contains(message)

        where:
        user     | pass     | client     | message
        'user'   | 'secret' | ''         | '[client_name] is required'
        'user'   | 'secret' | null       | '[client_name] is required'

        ''       | 'secret' | 'client'   | '[username] is required'
        null     | 'secret' | 'client'   | '[username] is required'

        'user'   | ''       | 'client'   | '[password] is required'
        'user'   | null     | 'client'   | '[password] is required'
    }

    void "invalid http methods for register client endpoint"() {
        expect:
        assertInvalidHttpMethods(registerClientUrl, ['get', 'put', 'delete'])
    }

    void "register a new client for an existing user"() {
        given:
        def request = new RestRequest(url: registerClientUrl, customizer: {
            username = TEST_USER
            password = TEST_PASSWORD
            client_name = 'some new client'
        })

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
        def tokenRequestForNewClient = new AccessTokenRequest(
                clientId: clientId,
                clientSecret: clientSecret,
                username: TEST_USER,
                password: TEST_PASSWORD,
                grantType: 'password',
                scope: [
                        'account-read', 'account-write',
                        'audiences-read', 'audiences-write',
                        'reels-read', 'reels-write',
                        'videos-read', 'videos-write'
                ]
        )

        and:
        def tokenRequestForExistingClient = new AccessTokenRequest(
                clientId: TEST_CLIENT_ID,
                clientSecret: TEST_CLIENT_SECRET,
                username: TEST_USER,
                password: TEST_PASSWORD,
                grantType: 'password',
                scope: [
                        'account-read', 'account-write',
                        'audiences-read', 'audiences-write',
                        'reels-read', 'reels-write',
                        'videos-read', 'videos-write'
                ]
        )

        when:
        def tokenForNewClient = getAccessTokenWithScope(tokenRequestForNewClient)
        def tokenForExistingClient = getAccessTokenWithScope(tokenRequestForExistingClient)

        then:
        tokenForNewClient != null
        tokenForExistingClient != null

        and:
        tokenForNewClient != tokenForExistingClient
    }

    private static getRegisterUrl() {
        getUrlForResource('account/register')
    }

    private static getRegisterClientUrl() {
        getUrlForResource('account/client')
    }

    private static getVerifyUrl() {
        getUrlForResource("account/confirm")
    }

    private static RestRequest createRegisterRequest(Closure params = null) {
        new RestRequest(url: registerUrl, customizer: params)
    }
}
