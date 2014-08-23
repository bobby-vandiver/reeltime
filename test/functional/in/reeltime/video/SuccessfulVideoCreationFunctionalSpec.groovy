package in.reeltime.video

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONObject

class SuccessfulVideoCreationFunctionalSpec extends FunctionalSpec {

    String uploadToken
    String reelsReadToken

    // TODO: Define what an acceptable turn around time
    private static final MAX_POLL_COUNT = 12
    private static final STATUS_RETRY_DELAY_IN_MILLIS = 5 * 1000

    void setup() {
        uploadToken = getAccessTokenWithScopeForTestUser('videos-write')
        reelsReadToken = getAccessTokenWithScopeForTestUser('reels-read')
    }

    void "minimum required params"() {
        given:
        def request = createUploadRequest(uploadToken) {
            reel = 'Uncategorized'
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

    void "uploaded video is added to the specified reel"() {
        given:
        def reelId = getUncategorizedReelId(reelsReadToken)
        def videoId = uploadVideo(uploadToken)

        expect:
        def list = listVideosInReel(reelId, reelsReadToken)
        assertVideoIdInList(list, videoId)
    }

    private RestRequest createUploadRequest(String token = null, Closure params = null) {
        new RestRequest(url: uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }
}
