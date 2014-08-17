package in.reeltime

import grails.util.BuildSettings
import helper.oauth2.AccessTokenRequest
import helper.oauth2.AccessTokenRequester
import helper.rest.AuthorizationAwareRestClient
import helper.test.ReelTimeClient
import helper.test.ReelTimeRequestFactory
import helper.test.ReelTimeUrlFactory
import helper.test.ResponseChecker
import spock.lang.Specification

abstract class FunctionalSpec extends Specification {

    private static final BASE_URL = System.getProperty(BuildSettings.FUNCTIONAL_BASE_URL_PROPERTY)

    @Delegate
    protected AuthorizationAwareRestClient restClient

    @Delegate
    protected ReelTimeClient reelTimeClient

    @Delegate
    protected ReelTimeRequestFactory requestFactory

    @Delegate
    protected ReelTimeUrlFactory urlFactory

    @Delegate
    protected ResponseChecker responseChecker

    protected static final TEST_USER = 'bob'
    protected static final TEST_PASSWORD = 'password'

    protected static final TEST_CLIENT_ID = 'test-client'
    protected static final TEST_CLIENT_SECRET = 'test-secret'

    void setup() {
        restClient = new AuthorizationAwareRestClient()
        responseChecker = new ResponseChecker(restClient)

        urlFactory = new ReelTimeUrlFactory(BASE_URL)

        requestFactory = new ReelTimeRequestFactory(urlFactory)
        reelTimeClient = new ReelTimeClient(restClient, requestFactory)
    }

    protected String getAccessTokenWithScopeForNonTestUser(String username, String scope) {
        def registrationResult = registerUser(username).json

        def accessRequest = new AccessTokenRequest(
                clientId: registrationResult.client_id,
                clientSecret: registrationResult.client_secret,
                grantType: 'password',
                username: username,
                password: TEST_PASSWORD,
                scope: [scope]
        )
        getAccessTokenWithScope(accessRequest)
    }

    protected String getAccessTokenWithScopeForNonTestUser(String scope) {
        def otherUsername = TEST_USER + 'a'
        return getAccessTokenWithScopeForNonTestUser(otherUsername, scope)
    }

    protected static String getAccessTokenWithScopes(Collection<String> scopes) {
        def request = new AccessTokenRequest(
                clientId: 'test-client',
                clientSecret: 'test-secret',
                grantType: 'password',
                username: TEST_USER,
                password: TEST_PASSWORD,
                scope: scopes
        )
        getAccessTokenWithScope(request)
    }

    // TODO: Specify user once user registration is implemented
    protected static String getAccessTokenWithScope(String scope) {
        def request = new AccessTokenRequest(
                clientId: TEST_CLIENT_ID,
                clientSecret: TEST_CLIENT_SECRET,
                grantType: 'password',
                username: TEST_USER,
                password: TEST_PASSWORD,
                scope: [scope]
        )
        getAccessTokenWithScope(request)
    }

    protected static String getAccessTokenWithScope(AccessTokenRequest request) {
        AccessTokenRequester.getAccessToken(request.params)
    }
}
