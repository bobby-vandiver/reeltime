package in.reeltime.video

import in.reeltime.test.rest.RestRequest
import in.reeltime.test.spec.FunctionalSpec

class InvalidVideoCreationFunctionalSpec extends FunctionalSpec {

    String invalidToken
    String videosReadToken
    String videosWriteToken
    String videosReadWriteToken

    File thumbnailFile

    void setup() {
        invalidToken = 'bad-mojo'
        videosReadToken = getAccessTokenWithScopeForTestUser('videos-read')
        videosWriteToken = getAccessTokenWithScopeForTestUser('videos-write')
        videosReadWriteToken = getAccessTokenWithScopesForTestUser(['videos-read', 'videos-write'])

        thumbnailFile = new File('src/test/resources/files/images/small.png')
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
        def request = createUploadRequest(videosReadToken)

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
        responseChecker.assertInvalidHttpMethods(urlFactory.uploadUrl, ['get', 'put', 'delete'], videosWriteToken)
    }

    void "all params are missing"() {
        given:
        def request = createUploadRequest(videosWriteToken)

        and:
        def expectedErrors = [
                '[video] is required',
                '[thumbnail] is required',
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
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'no-video'
            thumbnail = thumbnailFile
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[video] is required')
    }

    void "title param is missing"() {
        given:
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            video = new File('src/test/resources/files/videos/small.mp4')
            thumbnail = thumbnailFile
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[title] is required')
    }

    void "reel param is missing"() {
        given:
        def request = createUploadRequest(videosWriteToken) {
            title = 'no-reel'
            video = new File('src/test/resources/files/videos/small.mp4')
            thumbnail = thumbnailFile
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[reel] is required')
    }

    void "thumbnail param is missing"() {
        given:
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'no-reel'
            video = new File('src/test/resources/files/videos/small.mp4')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[thumbnail] is required')
    }

    void "unsupported thumbnail format"() {
        given:
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'no-reel'
            video = new File('src/test/resources/files/videos/small.mp4')
            thumbnail = new File('src/test/resources/files/images/maddox.jpg')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[thumbnail] must be png format')
    }

    void "unknown reel specified"() {
        given:
        def request = createUploadRequest(videosWriteToken) {
            reel = 'unknown-reel'
            title = 'unknown reel test'
            video = new File('src/test/resources/files/videos/small.mp4')
            thumbnail = thumbnailFile
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[reel] is unknown')
    }

    void "submitted video contains only aac stream"() {
        given:
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'video-is-only-aac'
            video = new File('src/test/resources/files/videos/sample_mpeg4.mp4')
            thumbnail = thumbnailFile
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
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'video-has-no-valid-streams'
            video = new File('src/test/resources/files/videos/empty')
            thumbnail = thumbnailFile
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertMultipleErrorMessagesResponse(response, 400, expected)
    }

    void "submitted video exceeds max length"() {
        given:
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'video-exceeds-max-length'
            video = new File('src/test/resources/files/videos/long_video_3_min_45_sec_17_MB.mp4')
            thumbnail = thumbnailFile
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[video] exceeds max length of 2 minutes')
    }

    void "submitted video exceeds max size"() {
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'video-exceeds-max-size'
            video = new File('src/test/resources/files/videos/exceeds_max_size.mp4')
            thumbnail = thumbnailFile
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, '[video] exceeds max size')
    }

    void "submitted thumbnail exceeds max size"() {
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'thumbnail-exceeds-max-size'
            video = new File('src/test/resources/files/videos/small.mp4')
            thumbnail = new File('src/test/resources/files/images/thumbnail_exceeds_max_size.mp4')
        }

        when:
        def response = post(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, '[thumbnail] exceeds max size')
    }

    void "invalid http method for video url"() {
        given:
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(videosReadWriteToken)

        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.getVideoUrl(videoId), ['post', 'put'], videosWriteToken)
    }

    void "cannot get unknown video"() {
        given:
        def request = requestFactory.getVideo(videosReadToken, 1234)

        when:
        def response = get(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 404, 'Requested video was not found')
    }

    private RestRequest createUploadRequest(String token = null, Closure params = null) {
        new RestRequest(url: urlFactory.uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }
}
