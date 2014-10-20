package in.reeltime.account

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
        responseChecker.assertErrorMessageInResponse(response, 403, message)

        where:
        username    |   message
        null        |   '[username] is required'
        ''          |   '[username] is required'
    }

    void "attempt to reset password for user with registered client who has not requested a reset"() {
        given:
        def request = requestFactory.resetPasswordForRegisteredClient(USERNAME, 'secret', 'reset', clientId, clientSecret)

        when:
        def response = post(request)

        then:
        responseChecker.assertStatusCode(response, 403)
    }
}
