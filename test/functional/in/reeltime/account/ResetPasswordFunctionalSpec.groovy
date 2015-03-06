package in.reeltime.account

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class ResetPasswordFunctionalSpec extends FunctionalSpec {

    static final String USERNAME = 'resetPassword'

    String clientId
    String clientSecret

    void setup() {
        def registrationResult = registerUser(USERNAME)
        clientId = registrationResult.json.client_id
        clientSecret = registrationResult.json.client_secret
    }

    @Unroll
    void "invalid http methods for url method [#urlMethod]"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory."${urlMethod}", ['get', 'put', 'delete'])

        where:
        _   |   urlMethod
        _   |   'sendResetPasswordEmailUrl'
        _   |   'resetPasswordUrl'
    }

    @Unroll
    void "username is required for sending reset password email -- cannot be [#username]"() {
        given:
        def request = requestFactory.sendResetPasswordEmail(username)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[username] is required')

        where:
        _   |   username
        _   |   null
        _   |   ''
    }

    @Unroll
    void "reset password errors for registered client -- new_password [#newPassword], code [#code]"() {
        given:
        def request = requestFactory.resetPasswordForRegisteredClient(USERNAME, newPassword, code, clientId, clientSecret)

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, message)

        where:
        newPassword     |   code        |   message
        null            |   'reset'     |   '[new_password] is required'
        ''              |   'reset'     |   '[new_password] is required'
        'abcde'         |   'reset'     |   '[new_password] must be at least 6 characters long'
        'secret'        |   null        |   '[code] is required'
        'secret'        |   ''          |   '[code] is required'
    }

    @Unroll
    void "reset password errors for registered client -- username [#username]"() {
        given:
        def request = requestFactory.resetPasswordForRegisteredClient(username, 'secret', 'reset', clientId, clientSecret)

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, message)

        where:
        username    |   message
        null        |   '[username] is required'
        ''          |   '[username] is required'
    }

    @Unroll
    void "reset password errors for registered client -- client_id [#id], client_secret [#secret]"() {
        given:
        def request = requestFactory.resetPasswordForRegisteredClient(USERNAME, 'secret', 'reset', id, secret)

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, message)

        where:
        id       |   secret     |   message
        null     |   'shh'      |   '[client_id] is required'
        ''       |   'shh'      |   '[client_id] is required'
        'secret' |   null       |   '[client_secret] is required'
        'secret' |   ''         |   '[client_secret] is required'
    }

    void "attempt to reset password for user with registered client who has not requested a reset"() {
        given:
        def request = requestFactory.resetPasswordForRegisteredClient(USERNAME, 'secret', 'reset', clientId, clientSecret)

        when:
        def response = post(request)

        then:
        responseChecker.assertUnauthorizedError(response)
    }

    void "authentic client not associated with user"() {
        given:
        registerUser('other')

        and:
        def request = requestFactory.resetPasswordForRegisteredClient('other', 'secret', 'reset', clientId, clientSecret)

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, 'Forbidden request')
    }

    void "invalid credentials for client"() {
        given:
        def request = requestFactory.resetPasswordForRegisteredClient(USERNAME, 'secret', 'reset', clientId, clientSecret + 'a')

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, 'Invalid credentials')
    }

    void "client_is_registered is missing"() {
        given:
        def request = new RestRequest(url: urlFactory.resetPasswordUrl)

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, '[client_is_registered] is required')
    }

    void "registered client missing all params"() {
        given:
        def expectedErrors = [
                '[username] is required',
                '[new_password] is required',
                '[code] is required',
                '[client_id] is required',
                '[client_secret] is required'
        ]

        and:
        def request = new RestRequest(url: urlFactory.resetPasswordUrl, customizer: {
            client_is_registered = true
        })

        when:
        def response = post(request)

        then:
        responseChecker.assertMultipleErrorMessagesResponse(response, 400, expectedErrors)
    }

    void "new client missing all params"() {
        given:
        def expectedErrors = [
                '[username] is required',
                '[new_password] is required',
                '[code] is required',
                '[client_name] is required'
        ]

        and:
        def request = new RestRequest(url: urlFactory.resetPasswordUrl, customizer: {
            client_is_registered = false
        })

        when:
        def response = post(request)

        then:
        responseChecker.assertMultipleErrorMessagesResponse(response, 400, expectedErrors)
    }

    @Unroll
    void "reset password errors for new client -- new_password [#newPassword], code [#code]"() {
        given:
        def request = requestFactory.resetPasswordForNewClient(USERNAME, newPassword, code, 'new client')

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, message)

        where:
        newPassword     |   code        |   message
        null            |   'reset'     |   '[new_password] is required'
        ''              |   'reset'     |   '[new_password] is required'
        'abcde'         |   'reset'     |   '[new_password] must be at least 6 characters long'
        'secret'        |   null        |   '[code] is required'
        'secret'        |   ''          |   '[code] is required'
    }

    @Unroll
    void "reset password errors for new client -- username [#username]"() {
        given:
        def request = requestFactory.resetPasswordForNewClient(username, 'secret', 'reset', 'new client')

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, message)

        where:
        username    |   message
        null        |   '[username] is required'
        ''          |   '[username] is required'
    }

    @Unroll
    void "reset password errors for new client -- client_name [#clientName]"() {
        given:
        def request = requestFactory.resetPasswordForNewClient(USERNAME, 'secret', 'reset', clientName)

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, message)

        where:
        clientName  |   message
        null        |   '[client_name] is required'
        ''          |   '[client_name] is required'
    }
}
