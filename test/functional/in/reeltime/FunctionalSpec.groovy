package in.reeltime

import grails.util.BuildSettings
import helper.oauth2.AccessTokenRequest
import helper.oauth2.AccessTokenRequester
import helper.rest.AuthorizationAwareRestClient
import helper.rest.RestRequest
import helper.test.ReelTimeClient
import helper.test.ReelTimeUrlFactory
import helper.test.ResponseChecker
import junit.framework.Assert
import spock.lang.Specification

abstract class FunctionalSpec extends Specification {

    private static final BASE_URL = System.getProperty(BuildSettings.FUNCTIONAL_BASE_URL_PROPERTY)

    // Video creation completion polling defaults
    private static final DEFAULT_MAX_POLL_COUNT = 12
    private static final DEFAULT_RETRY_DELAY_IN_MILLIS = 5 * 1000

    @Delegate
    protected AuthorizationAwareRestClient restClient

    @Delegate
    protected ReelTimeClient reelTimeClient

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
        reelTimeClient = new ReelTimeClient(restClient, urlFactory)
    }

    protected long uploadVideo(String token) {
        def request = new RestRequest(url: uploadUrl, token: token, isMultiPart: true, customizer: {
            title = 'minimum-viable-video'
            video = new File('test/files/small.mp4')
        })
        def response = post(request)
        assert response.status == 202
        return response.json.videoId
    }

    protected int pollForCreationComplete(long videoId, String uploadToken,
              int maxPollCount = DEFAULT_MAX_POLL_COUNT, long retryDelayMillis = DEFAULT_RETRY_DELAY_IN_MILLIS) {
        def request = createStatusRequest(videoId, uploadToken)

        int videoCreatedStatus = 0
        int pollCount = 0

        while(videoCreatedStatus != 201 && pollCount < maxPollCount) {
            def response = get(request)
            videoCreatedStatus = response.status

            if(videoCreatedStatus == 202) {
                println "Video [$videoId] is still being created. Sleeping for 5 seconds before next status query."
                sleep(retryDelayMillis)
            }
            pollCount++
        }
        return videoCreatedStatus
    }

    private RestRequest createStatusRequest(Long videoId, String token) {
        def statusUrl = getStatusUrl(videoId)
        new RestRequest(url: statusUrl, token: token)
    }

    protected Long getUncategorizedReelId(String token) {
        def request = createListReelsRequest(token)
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to list reels. Status: ${response.status} JSON: ${response.json}")
        }
        def uncategorizedReel = response.json.find { it.name == 'Uncategorized' }
        return uncategorizedReel.reelId
    }

    protected RestRequest createListReelsRequest(String token) {
        def reelsListUrl = getReelsListUrl(TEST_USER)
        new RestRequest(url: reelsListUrl, token: token)
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
