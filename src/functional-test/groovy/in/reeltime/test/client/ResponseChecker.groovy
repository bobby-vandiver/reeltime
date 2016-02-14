package in.reeltime.test.client

import in.reeltime.test.rest.HttpContentTypes
import in.reeltime.test.rest.HttpHeaders
import grails.plugins.rest.client.RestResponse
import in.reeltime.test.rest.AuthorizationAwareRestClient
import in.reeltime.test.rest.RestRequest
import org.grails.web.json.JSONElement

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
                assert response.status == 403 : "Expected 403 - response - status = ${response.status}, json = ${response?.json}"
                assert response.json.error == 'access_denied'
                assert response.json.error_description == 'Access is denied'
            }
            else {
                assert response.status == 401 : "Expected 401 - status = ${response.status}, json = ${response?.json}"

                // There's some weirdness with the json accessor on the RestResponse returning
                // null despite the HTTP response containing valid JSON
                if(method.toLowerCase() != 'put' && method.toLowerCase() != 'post') {
                    assert response.json.error == 'unauthorized'
                    assert response.json.error_description == 'Full authentication is required to access this resource'
                }
            }
        }
    }

    void assertInvalidPageNumbers(String url, String token) {
        def scenarios = [
                [page: -1, message: '[page] must be a positive number'],
                [page: 0, message: '[page] must be a positive number'],
                [page: 'abc', message: '[page] is invalid']
        ]

        scenarios.each { scenario ->
            def request = new RestRequest(url: url, token: token, queryParams: [page: scenario.page])
            def response = restClient.get(request)
            assertSingleErrorMessageResponse(response, 400, scenario.message)
        }
    }

    void assertSingleErrorMessageResponse(RestResponse response, int expectedStatus, String expectedMessage) {
        assertMultipleErrorMessagesResponse(response, expectedStatus, [expectedMessage])
    }

    void assertMultipleErrorMessagesResponse(RestResponse response, int expectedStatus, Collection<String> expectedErrors) {
        assertStatusCode(response, expectedStatus)
        assertContentType(response, HttpContentTypes.APPLICATION_JSON)

        assert response.json.errors.size() == expectedErrors.size()
        expectedErrors.each {
            assert response.json.errors.contains(it)
        }
    }

    void assertErrorMessageInResponse(RestResponse response, int expectedStatus, String expectedMessage) {
        assertStatusCode(response, expectedStatus)
        assertContentType(response, HttpContentTypes.APPLICATION_JSON)

        def matches = response.json.errors.findAll { it == expectedMessage }
        assert matches.size() == 1
    }

    void assertUnauthorizedError(RestResponse response) {
        assertErrorMessageInResponse(response, 403, 'Unauthorized operation requested')
    }

    void assertStatusCode(RestResponse response, int expected) {
        assert response.status == expected
    }

    void assertContentType(RestResponse response, String expected) {
        def contentType = response.headers.get(HttpHeaders.CONTENT_TYPE)[0]
        assert contentType.startsWith(expected)
    }

    void assertAuthJsonError(RestResponse response, int status, String error, String description) {
        assertStatusCode(response, status)
        assertContentType(response, HttpContentTypes.APPLICATION_JSON)

        assert response.json.error == error
        assert response.json.error_description == description
    }

    void assertAuthError(RestResponse response, int status, String error, String description) {
        assertStatusCode(response, status)
        assertContentType(response, HttpContentTypes.APPLICATION_JSON)

        def wwwAuthenticate = response.headers.get(HttpHeaders.WWW_AUTHENTICATE)[0]
        assert wwwAuthenticate.contains("error=\"$error\"")
        assert wwwAuthenticate.contains("error_description=\"$description\"")
    }

    void assertVideoIdInList(JSONElement list, Long videoId) {
        assert elementIsInList(list, 'video_id', videoId)
    }

    void assertVideoIdNotInList(JSONElement list, Long videoId) {
        assert !elementIsInList(list, 'video_id', videoId)
    }

    void assertReelIdInList(JSONElement list, Long reelId) {
        assert elementIsInList(list, 'reel_id', reelId)
    }

    void assertUsernameInList(JSONElement list, String username) {
        assert elementIsInList(list, 'username', username)
    }

    private boolean elementIsInList(JSONElement list, String key, Object value) {
        boolean found = false

        list.each { elem ->
            if(elem?."$key" == value) {
                found = true
            }
        }
        return found
    }
}
