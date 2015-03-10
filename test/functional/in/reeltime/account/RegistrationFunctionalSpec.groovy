package in.reeltime.account

import helper.oauth2.AccessTokenRequest
import helper.rest.RestRequest
import helper.test.EmailFormatter
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class RegistrationFunctionalSpec extends FunctionalSpec {

    String token

    void setup() {
        token = registerNewUserAndGetToken('registerTest', 'account-write')
    }

    @Unroll
    void "invalid http methods for register endpoint"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.registerUrl, ['get', 'put', 'delete'])
    }

    void "register a new user"() {
        given:
        def request = createRegisterRequest {
            email = 'someone@somewhere.com'
            username = 'newUser'
            password = 'n3wP4s$w0rd!'
            display_name = 'new user'
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

        cleanup:
        reelTimeClient.removeAccount(token)
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
            display_name = 'display'
            client_name = 'client'
        }

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors[0] == '[username] is not available'
    }

    void "email is not available"() {
        given:
        def name = 'someone'
        registerUser(name)

        and:
        def request = createRegisterRequest {
            email = EmailFormatter.emailForUsername(name)
            username = 'anyone'
            password = 'password'
            display_name = 'display'
            client_name = 'client'
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, '[email] is not available')
    }

    @Unroll
    void "invalid params email [#emailAddress], username [#user], password [#pass], display_name [#display], client_name [#client]"() {
        given:
        def request = createRegisterRequest {
            email = emailAddress
            username = user
            password = pass
            display_name = display
            client_name = client
        }

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors[0] == message

        where:
        emailAddress        |   user     | pass     |   display     |   client     |    message
        ''                  |   'user'   | 'secret' |   'display'   |   'client'   |    '[email] is required'
        null                |   'user'   | 'secret' |   'display'   |   'client'   |    '[email] is required'
        'test@'             |   'user'   | 'secret' |   'display'   |   'client'   |    '[email] is not a valid e-mail address'

        'test@reeltime.in'  |   'user'   | 'secret' |   'display'   |   ''         |    '[client_name] is required'
        'test@reeltime.in'  |   'user'   | 'secret' |   'display'   |   null       |    '[client_name] is required'

        'test@reeltime.in'  |   'user'   | 'secret' |   ''          |   'client'   |    '[display_name] is required'
        'test@reeltime.in'  |   'user'   | 'secret' |   null        |   'client'   |    '[display_name] is required'
        'test@reeltime.in'  |   'user'   | 'secret' |   'a'         |   'client'   |    '[display_name] must be 2-20 alphanumeric or space characters long'
        'test@reeltime.in'  |   'user'   | 'secret' |   'a' * 21    |   'client'   |    '[display_name] must be 2-20 alphanumeric or space characters long'


        'test@reeltime.in'  |   ''       | 'secret' |   'display'   |   'client'   |    '[username] is required'
        'test@reeltime.in'  |   null     | 'secret' |   'display'   |   'client'   |    '[username] is required'
        'test@reeltime.in'  |   'a'      | 'secret' |   'display'   |   'client'   |    '[username] must be 2-15 alphanumeric characters long'
        'test@reeltime.in'  |   '1234a!' | 'secret' |   'display'   |   'client'   |    '[username] must be 2-15 alphanumeric characters long'

        'test@reeltime.in'  |   'user'   | ''       |   'display'   |   'client'   |    '[password] is required'
        'test@reeltime.in'  |   'user'   | null     |   'display'   |   'client'   |    '[password] is required'
        'test@reeltime.in'  |   'user'   | 'short'  |   'display'   |   'client'   |    '[password] must be at least 6 characters long'
    }

    void "missing all params"() {
        given:
        def request = createRegisterRequest()

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 5

        and:
        response.json.errors.contains('[client_name] is required')
        response.json.errors.contains('[display_name] is required')
        response.json.errors.contains('[username] is required')
        response.json.errors.contains('[password] is required')
        response.json.errors.contains('[email] is required')
    }

    void "invalid http methods for confirmation endpoint"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.confirmationUrl, ['get', 'put', 'delete'], token)
    }

    @Unroll
    void "confirmation code is required -- cannot be [#code]"() {
        given:
        def request = requestFactory.confirmAccount(token, code)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[code] is required')

        where:
        _   |   code
        _   |   null
        _   |   ''
    }

    void "confirmation code is invalid" () {
        given:
        def request = requestFactory.confirmAccount(token, 'uh-oh')

        when:
        def response = post(request)

        then:
        responseChecker.assertUnauthorizedError(response)
    }

    void "register client with bad credentials"() {
        given:
        def request = new RestRequest(url: urlFactory.registerClientUrl, customizer: {
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
        def request = new RestRequest(url: urlFactory.registerClientUrl, customizer: {
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
        responseChecker.assertInvalidHttpMethods(urlFactory.registerClientUrl, ['get', 'put', 'delete'])
    }

    void "register a new client for an existing user"() {
        given:
        def request = new RestRequest(url: urlFactory.registerClientUrl, customizer: {
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
                clientId: testClientId,
                clientSecret: testClientSecret,
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

    private RestRequest createRegisterRequest(Closure params = null) {
        new RestRequest(url: urlFactory.registerUrl, customizer: params)
    }
}
