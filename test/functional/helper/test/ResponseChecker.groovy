package helper.test

import grails.plugins.rest.client.RestResponse
import helper.rest.AuthorizationAwareRestClient
import helper.rest.RestRequest
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONObject

import static helper.rest.HttpContentTypes.APPLICATION_JSON
import static helper.rest.HttpHeaders.CONTENT_TYPE
import static helper.rest.HttpHeaders.WWW_AUTHENTICATE

class ResponseChecker {

    private AuthorizationAwareRestClient restClient

    ResponseChecker(AuthorizationAwareRestClient restClient) {
        this.restClient = restClient
    }

    void assertInvalidHttpMethods(String url, Collection<String> methods, String token = null) {
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

    void assertSingleErrorMessageResponse(RestResponse response, int expectedStatus, String expectedMessage) {
        assertMultipleErrorMessagesResponse(response, expectedStatus, [expectedMessage])
    }

    void assertMultipleErrorMessagesResponse(RestResponse response, int expectedStatus, Collection<String> expectedErrors) {
        assertStatusCode(response, expectedStatus)
        assertContentType(response, APPLICATION_JSON)

        assert response.json.errors.size() == expectedErrors.size()
        expectedErrors.each {
            assert response.json.errors.contains(it)
        }
    }

    void assertStatusCode(RestResponse response, int expected) {
        assert response.status == expected
    }

    void assertContentType(RestResponse response, String expected) {
        def contentType = response.headers.get(CONTENT_TYPE)[0]
        assert contentType.startsWith(expected)
    }

    void assertAuthError(RestResponse response, int status, String error, String description) {
        assertStatusCode(response, status)
        assertContentType(response, APPLICATION_JSON)

        def wwwAuthenticate = response.headers.get(WWW_AUTHENTICATE)[0]
        assert wwwAuthenticate.contains("error=\"$error\"")
        assert wwwAuthenticate.contains("error_description=\"$description\"")
    }

    void assertVideoIdInList(JSONElement list, Long videoId) {
        boolean found = false
        JSONObject expected = [videoId: videoId]

        list.each { elem ->
            if(elem == expected) {
                found = true
            }
        }
        assert found
    }
}
