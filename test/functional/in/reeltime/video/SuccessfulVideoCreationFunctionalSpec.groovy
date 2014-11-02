package in.reeltime.video

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec

class SuccessfulVideoCreationFunctionalSpec extends FunctionalSpec {

    String videosReadToken
    String videosWriteToken
    String reelsReadToken

    // TODO: Define what an acceptable turn around time
    private static final MAX_POLL_COUNT = 12
    private static final STATUS_RETRY_DELAY_IN_MILLIS = 5 * 1000

    void setup() {
        videosReadToken = getAccessTokenWithScopeForTestUser('videos-read')
        videosWriteToken = getAccessTokenWithScopeForTestUser('videos-write')
        reelsReadToken = getAccessTokenWithScopeForTestUser('reels-read')
    }

    void "minimum required params"() {
        given:
        def request = createUploadRequest(videosWriteToken) {
            reel = 'Uncategorized'
            title = 'minimum-viable-video'
            video = new File('test/files/small.mp4')
        }

        when:
        def response = post(request)

        then:
        response.status == 202
        response.json.size() == 2
        response.json.video_id > 0
        response.json.title == 'minimum-viable-video'
    }

    void "successful upload polls for status"() {
        given:
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(videosWriteToken)

        expect:
        reelTimeClient.pollForCreationComplete(videosReadToken, videoId, MAX_POLL_COUNT, STATUS_RETRY_DELAY_IN_MILLIS) == 200
    }

    void "uploaded video is added to the specified reel"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(reelsReadToken)
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(videosWriteToken)

        expect:
        def list = reelTimeClient.listVideosInReel(reelsReadToken, reelId)
        responseChecker.assertVideoIdInList(list, videoId)
    }

    private RestRequest createUploadRequest(String token = null, Closure params = null) {
        new RestRequest(url: urlFactory.uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }
}
