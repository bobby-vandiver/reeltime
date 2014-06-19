package in.reeltime

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.util.BuildSettings
import helper.oauth2.AccessTokenRequester
import spock.lang.Shared
import spock.lang.Specification

abstract class FunctionalSpec extends Specification {

    // HTTP Headers
    protected static String AUTHORIZATION = 'Authorization'
    protected static String CONTENT_TYPE = 'Content-Type'
    protected static String WWW_AUTHENTICATE = 'WWW-Authenticate'

    // Content Types
    protected static String MULTI_PART_FORM_DATA = 'multipart/form-data'
    protected static String APPLICATION_JSON = 'application/json'

    private static final BASE_URL = System.getProperty(BuildSettings.FUNCTIONAL_BASE_URL_PROPERTY)

    @Shared
    RestBuilder restClient = new RestBuilder()

    protected String getEndpoint() {
        return BASE_URL + '/' + resource
    }

    abstract protected String getResource()

    // TODO: Specify user once user registration is implemented
    protected static String getAccessTokenWithScope(String scope) {
        def params = [
                client_id: 'test-client',
                client_secret: 'test-secret',
                grant_type: 'password',
                username: 'bob',
                password: 'pass',
                scope: scope
        ]
        AccessTokenRequester.getAccessToken(params)
    }

    protected RestResponse post() {
        post(null)
    }

    protected RestResponse post(String token) {
        post(token, null)
    }

    protected RestResponse post(String token, Closure customizer) {
        doPost(token, false, customizer)
    }

    protected RestResponse postFormData(String token, Closure customizer) {
        doPost(token, true, customizer)
    }

    private RestResponse doPost(String token, boolean isMultiPart, Closure customizer) {
        restClient.post(endpoint) {
            if(token) {
                header AUTHORIZATION, "Bearer $token"
            }
            if(isMultiPart) {
                contentType MULTI_PART_FORM_DATA
            }
            if(customizer) {
                customizer.delegate = delegate
                customizer()
            }
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
