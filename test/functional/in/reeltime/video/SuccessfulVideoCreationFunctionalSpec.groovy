package in.reeltime.video

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec

class SuccessfulVideoCreationFunctionalSpec extends FunctionalSpec {

    String uploadToken

    private static final MAX_POLL_COUNT = 5
    private static final STATUS_RETRY_DELAY_IN_MILLIS = 5 * 1000

    void setup() {
        uploadToken = getAccessTokenWithScope('upload')
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
        pollForCreationComplete(videoId) == 201
    }

    private int pollForCreationComplete(long videoId) {
        def request = createStatusRequest(videoId, uploadToken)

        int videoCreatedStatus = 0
        int pollCount = 0

        while(videoCreatedStatus != 201 && pollCount < MAX_POLL_COUNT) {
            def response = get(request)
            videoCreatedStatus = response.status

            if(videoCreatedStatus == 202) {
                println "Video [$videoId] is still being created. Sleeping for 5 seconds before next status query."
                sleep(STATUS_RETRY_DELAY_IN_MILLIS)
            }
            pollCount++
        }
        return videoCreatedStatus
    }

    private static getUploadUrl() {
        getUrlForResource('video')
    }

    private static getStatusUrl(Long videoId) {
        getUrlForResource("video/$videoId/status")
    }

    private static RestRequest createUploadRequest(String token = null, Closure params = null) {
        new RestRequest(url: uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }

    private static RestRequest createStatusRequest(Long videoId, String token) {
        def statusUrl = getStatusUrl(videoId)
        new RestRequest(url: statusUrl, token: token)
    }
}
