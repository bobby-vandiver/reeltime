package in.reeltime.video

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec

class InvalidVideoCreationFunctionalSpec extends FunctionalSpec {

    String invalidToken
    String insufficientScopeToken
    String uploadToken

    void setup() {
        invalidToken = 'bad-mojo'
        insufficientScopeToken = getAccessTokenWithScopeForTestUser('videos-read')
        uploadToken = getAccessTokenWithScopeForTestUser('videos-write')
    }

    void "no token present"() {
        given:
        def request = createUploadRequest()

        when:
        def response = post(request)

        then:
        responseChecker.assertAuthError(response, 401, 'unauthorized', 'Full authentication is required to access this resource')
    }

    void "token does not have upload scope"() {
        given:
        def request = createUploadRequest(insufficientScopeToken)

        when:
        def response = post(request)

        then:
        responseChecker.assertAuthJsonError(response, 403, 'access_denied', 'Access is denied')
    }

    void "invalid token"() {
        given:
        def request = createUploadRequest(invalidToken)

        when:
        def response = post(request)

        then:
        responseChecker.assertAuthError(response, 401, 'invalid_token', "Invalid access token: $invalidToken")
    }

    void "invalid http method for upload"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.uploadUrl, ['get', 'put', 'delete'], uploadToken)
    }

    void "all params are missing"() {
        given:
        def request = createUploadRequest(uploadToken)

        and:
        def expectedErrors = [
                '[video] is required',
                '[reel] is required',
                '[title] is required'
        ]

        when:
        def response = post(request)

        then:
        responseChecker.assertMultipleErrorMessagesResponse(response, 400, expectedErrors)
    }

    void "video param is missing"() {
        given:
        def request = createUploadRequest(uploadToken) {
            reel = 'Uncategorized'
            title = 'no-video'
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[video] is required')
    }

    void "title param is missing"() {
        given:
        def request = createUploadRequest(uploadToken) {
            reel = 'Uncategorized'
            video = new File('test/files/small.mp4')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[title] is required')
    }

    void "reel param is missing"() {
        given:
        def request = createUploadRequest(uploadToken) {
            title = 'no-reel'
            video = new File('test/files/small.mp4')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[reel] is required')
    }

    void "unknown reel specified"() {
        given:
        def request = createUploadRequest(uploadToken) {
            reel = 'unknown-reel'
            title = 'unknown reel test'
            video = new File('test/files/small.mp4')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[reel] is unknown')
    }

    void "submitted video contains only aac stream"() {
        given:
        def request = createUploadRequest(uploadToken) {
            reel = 'Uncategorized'
            title = 'video-is-only-aac'
            video = new File('test/files/sample_mpeg4.mp4')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[video] must contain an h264 video stream')
    }

    void "submitted video does not contain either h264 or aac streams"() {
        given:
        def expected = ['[video] must contain an h264 video stream', '[video] must contain an aac audio stream']

        and:
        def request = createUploadRequest(uploadToken) {
            reel = 'Uncategorized'
            title = 'video-has-no-valid-streams'
            video = new File('test/files/empty')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertMultipleErrorMessagesResponse(response, 400, expected)
    }

    void "submitted video exceeds max length"() {
        given:
        def request = createUploadRequest(uploadToken) {
            reel = 'Uncategorized'
            title = 'video-exceeds-max-length'
            video = new File('test/files/spidey.mp4')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[video] exceeds max length of 2 minutes')
    }

    void "invalid http method for status"() {
        given:
        def videoId = reelTimeClient.uploadVideo(uploadToken)

        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.getStatusUrl(videoId), ['post', 'put', 'delete'], uploadToken)
    }

    void "cannot check status of unknown video"() {
        given:
        def request = createStatusRequest(1234, uploadToken)

        when:
        def response = get(request)

        then:
        response.status == 404
    }

    void "cannot check status if not the creator"() {
        given:
        def videoId = reelTimeClient.uploadVideo(uploadToken)
        def differentUserToken = registerNewUserAndGetToken('notTheCreator', 'videos-write')

        and:
        def request = createStatusRequest(videoId, differentUserToken)

        when:
        def response = get(request)

        then:
        response.status == 403
    }

    private RestRequest createUploadRequest(String token = null, Closure params = null) {
        new RestRequest(url: urlFactory.uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }

    private RestRequest createStatusRequest(Long videoId, String token) {
        def statusUrl = urlFactory.getStatusUrl(videoId)
        new RestRequest(url: statusUrl, token: token)
    }
}
