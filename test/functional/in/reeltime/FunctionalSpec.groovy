package in.reeltime

import grails.plugins.rest.client.RestResponse
import grails.util.BuildSettings
import helper.oauth2.AccessTokenRequest
import helper.oauth2.AccessTokenRequester
import helper.rest.AuthorizationAwareRestClient
import helper.rest.RestRequest
import spock.lang.Specification

import static helper.rest.HttpContentTypes.*
import static helper.rest.HttpHeaders.*

abstract class FunctionalSpec extends Specification {

    private static final BASE_URL = System.getProperty(BuildSettings.FUNCTIONAL_BASE_URL_PROPERTY)

    @Delegate
    protected static AuthorizationAwareRestClient restClient = new AuthorizationAwareRestClient()

    protected static String getUrlForResource(String resource) {
        return BASE_URL + resource
    }

    // TODO: Specify user once user registration is implemented
    protected static String getAccessTokenWithScope(String scope) {
        def request = new AccessTokenRequest(
                clientId: 'test-client',
                clientSecret: 'test-secret',
                grantType: 'password',
                username: 'bob',
                password: 'pass',
                scope: [scope]
        )
        getAccessTokenWithScope(request)
    }

    protected static String getAccessTokenWithScope(AccessTokenRequest request) {
        AccessTokenRequester.getAccessToken(request.params)
    }

    protected static void assertInvalidHttpMethods(String url, Collection<String> methods, String token = null) {
        methods.each { String method ->
            def request = new RestRequest(url: url, token: token)
            def response = restClient."$method"(request) as RestResponse
            assert response.status == 405
            assert response.body == ''
        }
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
