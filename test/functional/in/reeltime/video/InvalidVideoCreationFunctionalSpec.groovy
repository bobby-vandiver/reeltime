package in.reeltime.video

import grails.plugins.rest.client.RestResponse
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class InvalidVideoCreationFunctionalSpec extends FunctionalSpec {

    String invalidToken
    String insufficientScopeToken
    String uploadToken

    @Override
    protected String getResource() {
        return 'video'
    }

    void setup() {
        invalidToken = 'bad-mojo'
        insufficientScopeToken = getAccessTokenWithScope('view')
        uploadToken = getAccessTokenWithScope('upload')
    }

    private static void assertErrorResponse(RestResponse response, Collection<String> expectedErrors) {
        assertStatusCode(response, 400)
        assertContentType(response, APPLICATION_JSON)

        assert response.json.errors.size() == expectedErrors.size()
        expectedErrors.each {
            assert response.json.errors.contains(it)
        }
    }

    void "no token present"() {
        when:
        def response = post()

        then:
        assertAuthError(response, 401, 'unauthorized', 'Full authentication is required to access this resource')
    }

    void "token does not have upload scope"() {
        when:
        def response = post(insufficientScopeToken)

        then:
        response.status == 403
        response.json.scope == 'upload'
        response.json.error == 'insufficient_scope'
        response.json.error_description == 'Insufficient scope for this resource'
    }

    void "invalid token"() {
        when:
        def response = post(invalidToken)

        then:
        assertAuthError(response, 401, 'invalid_token', "Invalid access token: $invalidToken")
    }

    @Unroll
    void "invalid http method [#method]"() {
        when:
        def response = "$method"(uploadToken)

        then:
        response.status == 405
        !response.json

        where:
        _   |   method
        _   |   'get'
        _   |   'put'
        _   |   'delete'
    }

    void "all params are missing"() {
        when:
        def response = post(uploadToken)

        then:
        assertErrorResponse(response, ['[video] is required', '[title] is required'])
    }

    void "video param is missing"() {
        when:
        def response = postFormData(uploadToken) {
            title = 'no-video'
        }

        then:
        assertErrorResponse(response, ['[video] is required'])
    }

    void "title param is missing"() {
        when:
        def response = postFormData(uploadToken) {
            video = new File('test/files/small.mp4')
        }

        then:
        assertErrorResponse(response, ['[title] is required'])
    }

    void "submitted video contains only aac stream"() {
        when:
        def response = postFormData(uploadToken) {
            title = 'video-is-only-aac'
            video = new File('test/files/sample_mpeg4.mp4')
        }

        then:
        assertErrorResponse(response, ['[video] must contain an h264 video stream'])
    }

    void "submitted video does not contain either h264 or aac streams"() {
        given:
        def expected = ['[video] must contain an h264 video stream', '[video] must contain an aac audio stream']

        when:
        def response = postFormData(uploadToken) {
            title = 'video-has-no-valid-streams'
            video = new File('test/files/empty')
        }

        then:
        assertErrorResponse(response, expected)
    }

    void "submitted video exceeds max length"() {
        when:
        def response = postFormData(uploadToken) {
            title = 'video-exceeds-max-length'
            video = new File('test/files/spidey.mp4')
        }

        then:
        assertErrorResponse(response, ['[video] exceeds max length of 2 minutes'])
    }
}
