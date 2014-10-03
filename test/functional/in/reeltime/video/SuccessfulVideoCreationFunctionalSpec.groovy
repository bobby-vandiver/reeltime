package in.reeltime.video

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec

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
        response.json.size() == 2
        response.json.videoId > 0
        response.json.title == 'minimum-viable-video'
    }

    void "successful upload polls for status"() {
        given:
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(uploadToken)

        expect:
        reelTimeClient.pollForCreationComplete(uploadToken, videoId, MAX_POLL_COUNT, STATUS_RETRY_DELAY_IN_MILLIS) == 201
    }

    void "uploaded video is added to the specified reel"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(reelsReadToken)
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(uploadToken)

        expect:
        def list = reelTimeClient.listVideosInReel(reelsReadToken, reelId)
        responseChecker.assertVideoIdInList(list, videoId)
    }

    private RestRequest createUploadRequest(String token = null, Closure params = null) {
        new RestRequest(url: urlFactory.uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }
}
