package in.reeltime.video

import in.reeltime.test.spec.FunctionalSpec

class SuccessfulVideoCreationFunctionalSpec extends FunctionalSpec {

    String videosToken
    String reelsReadToken

    void setup() {
        videosToken = getAccessTokenWithScopesForTestUser(['videos-read', 'videos-write'])
        reelsReadToken = getAccessTokenWithScopeForTestUser('reels-read')
    }

    void "minimum required params"() {
        given:
        def video = new File('src/test/resources/files/videos/small.mp4')
        def thumbnail = new File('src/test/resources/files/images/small.png')

        and:
        def request = requestFactory.uploadVideo(videosToken, 'minimum-viable-video', 'Uncategorized', video, thumbnail)

        when:
        def response = post(request)

        then:
        response.status == 202
        response.json.size() == 2
        response.json.video_id > 0
        response.json.title == 'minimum-viable-video'
    }

    void "uploaded video is added to the specified reel"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(reelsReadToken)
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(videosToken)

        expect:
        def list = reelTimeClient.listVideosInReel(reelsReadToken, reelId).videos
        responseChecker.assertVideoIdInList(list, videoId)
    }
}
