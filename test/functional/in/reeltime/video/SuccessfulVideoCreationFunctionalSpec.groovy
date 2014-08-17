package in.reeltime.video

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec

class SuccessfulVideoCreationFunctionalSpec extends FunctionalSpec {

    String uploadToken

    // TODO: Define what an acceptable turn around time
    private static final MAX_POLL_COUNT = 12
    private static final STATUS_RETRY_DELAY_IN_MILLIS = 5 * 1000

    void setup() {
        uploadToken = getAccessTokenWithScope('videos-write')
    }

    void "minimum required params"() {
        given:
        def request = createUploadRequest(uploadToken) {
            title = 'minimum-viable-video'
            video = new File('test/files/small.mp4')
        }

        when:
        def response = post(request)

        then:
        response.status == 202
        response.json.size() == 1
        response.json.videoId > 0
    }

    void "successful upload polls for status"() {
        given:
        def videoId = uploadVideo(uploadToken)

        expect:
        pollForCreationComplete(videoId, uploadToken, MAX_POLL_COUNT, STATUS_RETRY_DELAY_IN_MILLIS) == 201
    }

    private RestRequest createUploadRequest(String token = null, Closure params = null) {
        new RestRequest(url: uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }
}
