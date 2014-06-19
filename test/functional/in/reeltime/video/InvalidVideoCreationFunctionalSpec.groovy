package in.reeltime.video

import in.reeltime.FunctionalSpec

class InvalidVideoCreationFunctionalSpec extends FunctionalSpec {

    @Override
    protected String getResource() {
        return 'video'
    }

    void "no token present"() {
        when:
        def response = restClient.post(endpoint)

        then:
        assertAuthError(response, 401, 'unauthorized', 'Full authentication is required to access this resource')
    }

    void "token does not have upload scope"() {
        given:
        def token = getAccessTokenWithScope('view')

        when:
        def response = restClient.post(endpoint) {
            header AUTHORIZATION, "Bearer $token"
        }

        then:
        response.status == 403
        response.json.scope == 'upload'
        response.json.error == 'insufficient_scope'
        response.json.error_description == 'Insufficient scope for this resource'
    }

    void "invalid token"() {
        given:
        def token = 'bad-mojo'

        when:
        def response = restClient.post(endpoint) {
            header AUTHORIZATION, "Bearer $token"
        }

        then:
        assertAuthError(response, 401, 'invalid_token', 'Invalid access token: bad-mojo')
    }

    void "all params are missing"() {
        given:
        def token = getAccessTokenWithScope('upload')

        when:
        def response = restClient.post(endpoint) {
            header AUTHORIZATION, "Bearer $token"
        }

        then:
        response.status == 400
        response.json.errors.size() == 2
        response.json.errors.contains('[video] is required')
        response.json.errors.contains('[title] is required')
    }

    void "video param is missing"() {
        given:
        def token = getAccessTokenWithScope('upload')

        when:
        def response = restClient.post(endpoint) {
            header AUTHORIZATION, "Bearer $token"
            contentType MULTI_PART_FORM_DATA
            title = 'no-video'
        }

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors.contains('[video] is required')
    }

    void "title param is missing"() {
        given:
        def token = getAccessTokenWithScope('upload')

        when:
        def response = restClient.post(endpoint) {
            header AUTHORIZATION, "Bearer $token"
            contentType MULTI_PART_FORM_DATA
            video = new File('test/files/small.mp4')
        }

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors.contains('[title] is required')
    }

    void "submitted video contains only aac stream"() {
        given:
        def token = getAccessTokenWithScope('upload')

        when:
        def response = restClient.post(endpoint) {
            header AUTHORIZATION, "Bearer $token"
            contentType MULTI_PART_FORM_DATA

            title = 'video-is-only-aac'
            video = new File('test/files/sample_mpeg4.mp4')
        }

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors.contains('[video] must contain an h264 video stream')
    }

    void "submitted video does not contain either h264 or aac streams"() {
        given:
        def token = getAccessTokenWithScope('upload')

        when:
        def response = restClient.post(endpoint) {
            header AUTHORIZATION, "Bearer $token"
            contentType MULTI_PART_FORM_DATA

            title = 'video-has-no-valid-streams'
            video = new File('test/files/empty')
        }

        then:
        response.status == 400
        response.json.errors.size() == 2
        response.json.errors.contains('[video] must contain an h264 video stream')
        response.json.errors.contains('[video] must contain an aac audio stream')
    }

    void "submitted video exceeds max length"() {
        given:
        def token = getAccessTokenWithScope('upload')

        when:
        def response = restClient.post(endpoint) {
            header AUTHORIZATION, "Bearer $token"
            contentType MULTI_PART_FORM_DATA

            title = 'video-exceeds-max-length'
            video = new File('test/files/spidey.mp4')
        }

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors.contains('[video] exceeds max length of 2 minutes')
    }
}
