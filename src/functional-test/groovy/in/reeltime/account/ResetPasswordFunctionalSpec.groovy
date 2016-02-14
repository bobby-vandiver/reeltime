package in.reeltime.account

import in.reeltime.test.rest.RestRequest
import in.reeltime.test.spec.FunctionalSpec
import spock.lang.IgnoreIf
import spock.lang.Unroll

import static in.reeltime.common.ContentTypes.getAPPLICATION_JSON

class ResetPasswordFunctionalSpec extends FunctionalSpec {

    static final String USERNAME = 'resetPassword'

    private static final String OLD_PASSWORD = 'oldPassword'
    private static final String NEW_PASSWORD = 'newPassword'

    private static final String NEW_CLIENT_NAME = 'new client'

    String clientId
    String clientSecret

    String token

    void setup() {
        def registrationResult = registerUser(USERNAME, OLD_PASSWORD)

        clientId = registrationResult.json.client_id
        clientSecret = registrationResult.json.client_secret

        token = getAccessTokenWithScopes(USERNAME, OLD_PASSWORD, clientId, clientSecret, ALL_SCOPES)
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
        'secret'        |   'invalid'   |   '[code] is invalid'
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
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[code] is invalid')
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
            client_is_registered = true.toString()
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
            client_is_registered = false.toString()
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
        'secret'        |   'invalid'   |   '[code] is invalid'
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

    @IgnoreIf({ !FunctionalSpec.isLocalFunctionalTest() })
    void "successfully reset password for previously registered client"() {
        given:
        def request = requestFactory.resetPasswordForRegisteredClient(USERNAME, NEW_PASSWORD,
                passwordResetCode, clientId, clientSecret)

        when:
        def response = post(request)

        then:
        responseChecker.assertStatusCode(response, 200)

        and:
        updateUserPassword(USERNAME, NEW_PASSWORD)

        when:
        response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, '[code] is invalid')

        expect:
        getAccessTokenWithScopes(USERNAME, NEW_PASSWORD, clientId, clientSecret, ALL_SCOPES) != null
    }

    @IgnoreIf({ !FunctionalSpec.isLocalFunctionalTest() })
    void "successfully reset for a new client"() {
        given:
        def request = requestFactory.resetPasswordForNewClient(USERNAME, NEW_PASSWORD,
                passwordResetCode, NEW_CLIENT_NAME)

        when:
        def response = post(request)

        then:
        responseChecker.assertStatusCode(response, 201)
        responseChecker.assertContentType(response, APPLICATION_JSON)

        and:
        updateUserPassword(USERNAME, NEW_PASSWORD)

        and:
        def json = response.json
        json.size() == 2

        and:
        def newClientId = json.client_id
        def newClientSecret = json.client_secret

        expect:
        getAccessTokenWithScopes(USERNAME, NEW_PASSWORD, newClientId, newClientSecret, ALL_SCOPES) != null
    }


    private String getPasswordResetCode() {
        String confirmationCode = emailReader.accountConfirmationCode(USERNAME)
        reelTimeClient.confirmAccount(token, confirmationCode)

        reelTimeClient.sendResetPasswordEmail(USERNAME)
        return emailReader.resetPasswordCode(USERNAME)
    }
}
