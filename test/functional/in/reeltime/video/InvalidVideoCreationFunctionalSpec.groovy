package in.reeltime.video

import grails.plugins.rest.client.RestResponse
import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

import static helper.rest.HttpContentTypes.*

class InvalidVideoCreationFunctionalSpec extends FunctionalSpec {

    String invalidToken
    String insufficientScopeToken
    String uploadToken

    void setup() {
        invalidToken = 'bad-mojo'
        insufficientScopeToken = getAccessTokenWithScope('view')
        uploadToken = getAccessTokenWithScope('upload')
    }

    void "no token present"() {
        given:
        def request = createRequest()

        when:
        def response = post(request)

        then:
        assertAuthError(response, 401, 'unauthorized', 'Full authentication is required to access this resource')
    }

    void "token does not have upload scope"() {
        given:
        def request = createRequest(insufficientScopeToken)

        when:
        def response = post(request)

        then:
        response.status == 403
        response.json.scope == 'upload'
        response.json.error == 'insufficient_scope'
        response.json.error_description == 'Insufficient scope for this resource'
    }

    void "invalid token"() {
        given:
        def request = createRequest(invalidToken)

        when:
        def response = post(request)

        then:
        assertAuthError(response, 401, 'invalid_token', "Invalid access token: $invalidToken")
    }

    @Unroll
    void "invalid http method [#method]"() {
        expect:
        assertInvalidHttpMethods(uploadUrl, ['get', 'put', 'delete'], uploadToken)
    }

    void "all params are missing"() {
        given:
        def request = createRequest(uploadToken)

        when:
        def response = post(request)

        then:
        assertErrorResponse(response, ['[video] is required', '[title] is required'])
    }

    void "video param is missing"() {
        given:
        def request = createRequest(uploadToken) {
            title = 'no-video'
        }

        when:
        def response = post(request)

        then:
        assertErrorResponse(response, ['[video] is required'])
    }

    void "title param is missing"() {
        given:
        def request = createRequest(uploadToken) {
            video = new File('test/files/small.mp4')
        }

        when:
        def response = post(request)

        then:
        assertErrorResponse(response, ['[title] is required'])
    }

    void "submitted video contains only aac stream"() {
        given:
        def request = createRequest(uploadToken) {
            title = 'video-is-only-aac'
            video = new File('test/files/sample_mpeg4.mp4')
        }

        when:
        def response = post(request)

        then:
        assertErrorResponse(response, ['[video] must contain an h264 video stream'])
    }

    void "submitted video does not contain either h264 or aac streams"() {
        given:
        def expected = ['[video] must contain an h264 video stream', '[video] must contain an aac audio stream']

        and:
        def request = createRequest(uploadToken) {
            title = 'video-has-no-valid-streams'
            video = new File('test/files/empty')
        }

        when:
        def response = post(request)

        then:
        assertErrorResponse(response, expected)
    }

    void "submitted video exceeds max length"() {
        given:
        def request = createRequest(uploadToken) {
            title = 'video-exceeds-max-length'
            video = new File('test/files/spidey.mp4')
        }

        when:
        def response = post(request)

        then:
        assertErrorResponse(response, ['[video] exceeds max length of 2 minutes'])
    }

    private static getUploadUrl() {
        getUrlForResource('video')
    }

    private static RestRequest createRequest(String token = null, Closure params = null) {
        new RestRequest(url: uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }

    private static void assertErrorResponse(RestResponse response, Collection<String> expectedErrors) {
        assertStatusCode(response, 400)
        assertContentType(response, APPLICATION_JSON)

        assert response.json.errors.size() == expectedErrors.size()
        expectedErrors.each {
            assert response.json.errors.contains(it)
        }
    }
}
