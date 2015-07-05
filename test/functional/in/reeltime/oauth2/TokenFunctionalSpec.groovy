package in.reeltime.oauth2

import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class TokenFunctionalSpec extends FunctionalSpec {

    String token

    void setup() {
        token = registerNewUserAndGetToken('tokenTests', ALL_SCOPES)
    }

    void "invalid http methods for token revocation endpoint"() {
        given:
        def url = urlFactory.getTokenRevocationUrl()

        expect:
        responseChecker.assertInvalidHttpMethods(url, ['get', 'put', 'delete'])
    }

    @Unroll
    void "missing token [#tokenToRevoke]"() {
        given:
        def request = requestFactory.revokeToken(token, tokenToRevoke)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[access_token] is required')

        where:
        _   |   tokenToRevoke
        _   |   null
        _   |   ''
    }

    void "invalid token should not leak the fact that it is invalid"() {
        given:
        def request = requestFactory.revokeToken(token, 'nonsense')

        when:
        def response = post(request)

        then:
        responseChecker.assertStatusCode(response, 200)
    }

    void "revoke token"() {
        given:
        def request = requestFactory.revokeToken(token, token)

        when:
        def response = post(request)

        then:
        responseChecker.assertStatusCode(response, 200)

        and:
        assertTokenIsInvalid(token)
    }

    private void assertTokenIsInvalid(String token) {
        def request = requestFactory.removeAccount(token)
        def response = delete(request)

        def description = "Invalid access token: $token"
        responseChecker.assertAuthError(response, 401, 'invalid_token', description)
    }
}
