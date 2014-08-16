package in.reeltime

import grails.plugins.rest.client.RestResponse
import grails.util.BuildSettings
import groovy.json.JsonSlurper
import helper.oauth2.AccessTokenRequest
import helper.oauth2.AccessTokenRequester
import helper.rest.AuthorizationAwareRestClient
import helper.rest.RestRequest
import junit.framework.Assert
import org.codehaus.groovy.grails.web.json.JSONElement
import spock.lang.Specification

import static helper.rest.HttpContentTypes.*
import static helper.rest.HttpHeaders.*

abstract class FunctionalSpec extends Specification {

    private static final BASE_URL = System.getProperty(BuildSettings.FUNCTIONAL_BASE_URL_PROPERTY)

    // Video creation completion polling defaults
    private static final DEFAULT_MAX_POLL_COUNT = 12
    private static final DEFAULT_RETRY_DELAY_IN_MILLIS = 5 * 1000

    @Delegate
    protected static AuthorizationAwareRestClient restClient = new AuthorizationAwareRestClient()

    protected static String getUrlForResource(String resource) {
        return BASE_URL + resource
    }

    protected static final TEST_USER = 'bob'
    protected static final TEST_PASSWORD = 'password'

    protected static final TEST_CLIENT_ID = 'test-client'
    protected static final TEST_CLIENT_SECRET = 'test-secret'

    protected JSONElement registerUser(String name) {
        def url = getUrlForResource('account/register')
        def request = new RestRequest(url: url, customizer: {
            email = name + '@test.com'
            username = name
            password = TEST_PASSWORD
            client_name = 'client'
        })
        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to register user. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json
    }

    protected long uploadVideo(String token) {
        def url = getUrlForResource('video')
        def request = new RestRequest(url: url, token: token, isMultiPart: true, customizer: {
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

    private static RestRequest createStatusRequest(Long videoId, String token) {
        def statusUrl = getStatusUrl(videoId)
        new RestRequest(url: statusUrl, token: token)
    }

    private static getStatusUrl(Long videoId) {
        getUrlForResource("video/$videoId/status")
    }

    protected static Long getUncategorizedReelId(String token) {
        def request = createListReelsRequest(token)
        def response = restClient.get(request)

        if(response.status != 200) {
            Assert.fail("Failed to list reels. Status: ${response.status} JSON: ${response.json}")
        }
        def uncategorizedReel = response.json.find { it.name == 'Uncategorized' }
        return uncategorizedReel.reelId
    }

    protected static RestRequest createListReelsRequest(String token) {
        def reelsListUrl = getUrlForResource("/user/$TEST_USER/reels")
        new RestRequest(url: reelsListUrl, token: token)
    }

    protected String getAccessTokenWithScopeForNonTestUser(String username, String scope) {
        def registrationResult = registerUser(username)

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

    protected static void assertInvalidHttpMethods(String url, Collection<String> methods, String token = null) {
        methods.each { String method ->
            println "HTTP Method: $method"
            def request = new RestRequest(url: url, token: token)
            def response = restClient."$method"(request) as RestResponse

            if(token) {
                assert response.status == 403
                assert response.json.error == 'access_denied'
                assert response.json.error_description == 'Access is denied'
            }
            else {
                assert response.status == 401

                // There's some weirdness with the json accessor on the RestResponse returning
                // null despite the HTTP response containing valid JSON
                if(method.toLowerCase() != 'put') {
                    assert response.json.error == 'unauthorized'
                    assert response.json.error_description == 'Full authentication is required to access this resource'
                }
            }
        }
    }

    protected static void assertSingleErrorMessageResponse(RestResponse response, int expectedStatus, String expectedMessage) {
        assert response.status == expectedStatus
        assert response.json.errors.size() == 1
        assert response.json.errors[0] == expectedMessage
    }

    protected static void assertStatusCode(RestResponse response, int expected) {
        assert response.status == expected
    }

    protected static void assertContentType(RestResponse response, String expected) {
        def contentType = response.headers.get(CONTENT_TYPE)[0]
        assert contentType.startsWith(expected)
    }

    protected static void assertAuthError(RestResponse response, int status, String error, String description) {
        assertStatusCode(response, status)
        assertContentType(response, APPLICATION_JSON)

        def wwwAuthenticate = response.headers.get(WWW_AUTHENTICATE)[0]
        assert wwwAuthenticate.contains("error=\"$error\"")
        assert wwwAuthenticate.contains("error_description=\"$description\"")
    }
}
