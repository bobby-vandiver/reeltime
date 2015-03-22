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
        def url = urlFactory.getTokenRevocationUrl(token)

        expect:
        responseChecker.assertInvalidHttpMethods(url, ['get', 'post', 'put'])
    }

    void "revoke token"() {
        given:
        def request = requestFactory.revokeToken(token, token)

        when:
        def response = delete(request)

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
