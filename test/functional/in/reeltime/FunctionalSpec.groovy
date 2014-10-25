package in.reeltime

import grails.plugins.rest.client.RestResponse
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

    protected ReelTimeClient reelTimeClient

    protected ReelTimeRequestFactory requestFactory
    protected ReelTimeUrlFactory urlFactory

    protected ResponseChecker responseChecker

    protected static final String TEST_USER = 'bob'
    protected static final String TEST_PASSWORD = 'password'

    protected static final String TEST_CLIENT_NAME = 'test-client-name'

    protected static final List<String> ALL_SCOPES = [
        'account-read', 'account-write',
        'audiences-read', 'audiences-write',
        'reels-read', 'reels-write',
        'users-read', 'users-write',
        'videos-read', 'videos-write'
    ]

    protected static final String CREATE_REEL_ACTIVITY_TYPE = 'create-reel'
    protected static final String JOIN_REEL_AUDIENCE_ACTIVITY_TYPE = 'join-reel-audience'
    protected static final String ADD_VIDEO_TO_REEL_ACTIVITY_TYPE = 'add-video-to-reel'

    protected testClientId
    protected testClientSecret

    private Collection<Map> registeredTestUsers

    void setup() {
        restClient = new AuthorizationAwareRestClient()
        responseChecker = new ResponseChecker(restClient)

        urlFactory = new ReelTimeUrlFactory(BASE_URL)

        requestFactory = new ReelTimeRequestFactory(urlFactory)
        reelTimeClient = new ReelTimeClient(restClient, requestFactory)

        registeredTestUsers = []
        registerTestUser()
    }

    private void registerTestUser() {
        def registrationResult = registerUser(TEST_USER).json

        testClientId = registrationResult.client_id
        testClientSecret = registrationResult.client_secret
    }

    void cleanup() {
        registeredTestUsers.each { Map entry ->
            removeTestUser(entry.username, entry.clientId, entry.clientSecret)
        }
    }

    private void removeTestUser(String username, String clientId, String clientSecret) {
        println "Removing account for [$username]"
        def request = createAccessTokenRequest(username, clientId, clientSecret, ['account-write'])

        def token = getAccessTokenWithScope(request)
        reelTimeClient.removeAccount(token)
    }

    protected RestResponse registerUser(String username, String password = TEST_PASSWORD) {
        def response = reelTimeClient.registerUser(username, password, TEST_CLIENT_NAME)

        def clientId = response.json.client_id
        def clientSecret = response.json.client_secret

        registeredTestUsers << [username:username, clientId: clientId, clientSecret: clientSecret]
        return response
    }

    protected String registerNewUserAndGetToken(String username, String scope) {
        registerNewUserAndGetToken(username, [scope])
    }

    protected String registerNewUserAndGetToken(String username, Collection<String> scopes) {
        def registrationResult = registerUser(username).json

        def clientId = registrationResult.client_id
        def clientSecret = registrationResult.client_secret

        def accessRequest = createAccessTokenRequest(username, clientId, clientSecret, scopes)
        getAccessTokenWithScope(accessRequest)
    }

    protected String getAccessTokenWithScopeForTestUser(String scope) {
        getAccessTokenWithScopesForTestUser([scope])
    }

    protected String getAccessTokenWithScopesForTestUser(Collection<String> scopes) {
        def request = createAccessTokenRequest(TEST_USER, testClientId, testClientSecret, scopes)
        getAccessTokenWithScope(request)
    }

    private static AccessTokenRequest createAccessTokenRequest(String username, String clientId, String clientSecret,
                                                               Collection<String> scopes) {
        new AccessTokenRequest(
                clientId: clientId,
                clientSecret: clientSecret,
                grantType: 'password',
                username: username,
                password: TEST_PASSWORD,
                scope: scopes
        )
    }

    protected static String getAccessTokenWithScope(AccessTokenRequest request) {
        AccessTokenRequester.getAccessToken(request.params)
    }
}
