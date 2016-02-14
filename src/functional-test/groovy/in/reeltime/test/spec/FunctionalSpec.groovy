package in.reeltime.test.spec

import in.reeltime.test.client.ResponseChecker
import in.reeltime.test.util.EmailReader
import grails.plugins.rest.client.RestResponse
import in.reeltime.test.oauth2.AccessTokenRequest
import in.reeltime.test.oauth2.AccessTokenRequester
import in.reeltime.test.rest.AuthorizationAwareRestClient
import in.reeltime.test.client.ReelTimeClient
import in.reeltime.test.client.ReelTimeRequestFactory
import in.reeltime.test.client.ReelTimeUrlFactory
import spock.lang.Specification
import in.reeltime.test.config.EnvironmentConfiguration

abstract class FunctionalSpec extends Specification {

    private static final String BASE_URL = EnvironmentConfiguration.getBaseUrl()

    @Delegate
    protected AuthorizationAwareRestClient restClient

    protected ReelTimeClient reelTimeClient

    protected ReelTimeRequestFactory requestFactory
    protected ReelTimeUrlFactory urlFactory

    protected ResponseChecker responseChecker
    protected EmailReader emailReader = new EmailReader()

    protected static final String TEST_USER = 'bob'
    protected static final String TEST_PASSWORD = 'password'

    protected static final String TEST_CLIENT_NAME = 'test-client-name'
    protected static final String TEST_DISPLAY_NAME = 'bob-display'

    protected static final List<String> ALL_SCOPES = [
        'account-read', 'account-write',
        'audiences-read', 'audiences-write',
        'reels-read', 'reels-write',
        'users-read', 'users-write',
        'videos-read', 'videos-write'
    ]

    protected static final List<String> USERS_SCOPES = [
        'users-read', 'users-write',
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

    void cleanup() {
        registeredTestUsers.each { Map entry ->
            removeTestUser(entry.username, entry.password, entry.clientId, entry.clientSecret)
        }
    }

    static boolean isLocalFunctionalTest() {
        return EnvironmentConfiguration.isLocalEnvironment()
    }

    static boolean isAcceptanceTest() {
        return EnvironmentConfiguration.isAcceptanceEnvironment()
    }

    protected void registerTestUser() {
        def registrationResult = registerUser(TEST_USER).json

        testClientId = registrationResult.client_id
        testClientSecret = registrationResult.client_secret
    }

    protected void removeTestUser(String username, String password, String clientId, String clientSecret) {
        println "Removing account for [$username]"

        def request = createAccessTokenRequest(username, clientId, clientSecret, ['account-write'])
        request.password = password

        def token = getAccessTokenWithScope(request)
        reelTimeClient.removeAccount(token)
    }

    protected Map getClientCredentialsForRegisteredUser(String username) {
        def entry = registeredTestUsers.find { it?.username == username }
        if(!entry) {
            throw new IllegalArgumentException("Unknown user: $username")
        }
        [clientId: entry.clientId, clientSecret: entry.clientSecret]
    }

    protected void updateUserPassword(String username, String newPassword) {
        def entry = registeredTestUsers.find { it?.username == username }
        if(!entry) {
            throw new IllegalArgumentException("Unknown user: $username")
        }
        entry.password = newPassword
    }

    protected RestResponse registerUser(String username, String password = TEST_PASSWORD, String displayName = null) {
        displayName = displayName ?: username

        println "Registering user with username [$username], password [$password] and display name [$displayName]"
        def response = reelTimeClient.registerUser(username, password, TEST_CLIENT_NAME, displayName)

        def clientId = response.json.client_id
        def clientSecret = response.json.client_secret

        registeredTestUsers << [username:username, password: password, clientId: clientId, clientSecret: clientSecret]
        return response
    }

    protected String registerNewUserAndGetToken(String username, String scope) {
        registerNewUserAndGetToken(username, [scope])
    }

    protected String registerNewUserAndGetToken(String username, String password, String displayName,
                                                Collection<String> scopes) {
        def registrationResult = registerUser(username, password, displayName).json

        def clientId = registrationResult.client_id
        def clientSecret = registrationResult.client_secret

        def accessRequest = createAccessTokenRequest(username, clientId, clientSecret, scopes)
        accessRequest.password = password
        getAccessTokenWithScope(accessRequest)
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

    protected String getAccessTokenWithScopes(String username, String password, String clientId, String clientSecret,
                                              Collection<String> scopes) {
        def request = createAccessTokenRequest(username, password, clientId, clientSecret, scopes)
        getAccessTokenWithScope(request)
    }

    protected static AccessTokenRequest createAccessTokenRequest(String username, String clientId, String clientSecret,
                                                               Collection<String> scopes) {
        createAccessTokenRequest(username, TEST_PASSWORD, clientId, clientSecret, scopes)
    }

    protected static AccessTokenRequest createAccessTokenRequest(String username, String password,
                                                                 String clientId, String clientSecret,
                                                                 Collection<String> scopes) {
        new AccessTokenRequest(
                clientId: clientId,
                clientSecret: clientSecret,
                grantType: 'password',
                username: username,
                password: password,
                scope: scopes
        )
    }

    protected static String getAccessTokenWithScope(AccessTokenRequest request) {
        AccessTokenRequester.getAccessToken(request.params)
    }
}
