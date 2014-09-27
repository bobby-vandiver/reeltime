package in.reeltime.video

import in.reeltime.FunctionalSpec

class VideoRemovalFunctionalSpec extends FunctionalSpec {

    String videosReadToken
    String videosWriteToken
    String reelsReadToken

    void setup() {
        videosReadToken = getAccessTokenWithScopeForTestUser('videos-read')
        videosWriteToken = getAccessTokenWithScopeForTestUser('videos-write')
        reelsReadToken = getAccessTokenWithScopeForTestUser('reels-read')
    }

    void "no token present"() {
        given:
        def request = requestFactory.deleteVideo(null, 1234)

        when:
        def response = delete(request)

        then:
        responseChecker.assertAuthError(response, 401, 'unauthorized', 'Full authentication is required to access this resource')
    }

    void "token must grant write access"() {
        given:
        def request = requestFactory.deleteVideo(videosReadToken, 1234)

        when:
        def response = delete(request)

        then:
        responseChecker.assertAuthJsonError(response, 403, 'access_denied', 'Access is denied')
    }

    void "invalid http method for video removal"() {
        given:
        def deleteUrl = urlFactory.getDeleteVideoUrl(1243)

        expect:
        responseChecker.assertInvalidHttpMethods(deleteUrl, ['get', 'put', 'post'], videosWriteToken)
    }

    void "videoId param is missing"() {
        given:
        def request = requestFactory.deleteVideo(videosWriteToken, null)

        when:
        def response = delete(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[videoId] is required')
    }

    void "successfully delete video"() {
        given:
        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(reelsReadToken)
        def videoId = reelTimeClient.uploadVideo(videosWriteToken)

        def beforeList = reelTimeClient.listVideosInReel(uncategorizedReelId, reelsReadToken)
        responseChecker.assertVideoIdInList(beforeList, videoId)

        and:
        def request = requestFactory.deleteVideo(videosWriteToken, videoId)

        when:
        def response = delete(request)

        then:
        response.status == 200

        and:
        def afterList = reelTimeClient.listVideosInReel(uncategorizedReelId, reelsReadToken)
        responseChecker.assertVideoIdNotInList(afterList, videoId)
    }
}
