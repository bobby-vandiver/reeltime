package in.reeltime.video

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class VideoFunctionalSpec extends FunctionalSpec {

    String token

    void setup() {
        token = registerNewUserAndGetToken('listVideos', ['videos-read', 'videos-write', 'reels-write'])
    }

    void "invalid http methods"() {
        given:
        def url = urlFactory.listVideosUrl

        expect:
        responseChecker.assertInvalidHttpMethods(url, ['post', 'put', 'delete'], token)
    }

    @Unroll
    void "invalid page [#page] requested"() {
        given:
        def request = new RestRequest(url: urlFactory.listVideosUrl, token: token, queryParams: [page: page])

        when:
        def response = get(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, message)

        where:
        page    |   message
        -1      |   '[page] must be a positive number'
        0       |   '[page] must be a positive number'
        'abc'   |   '[page] is invalid'
    }

    void "no videos have been uploaded"() {
        when:
        def list = reelTimeClient.listVideos(token)

        then:
        list.size() == 0
    }

    void "upload and list a few videos"() {
        given:
        def firstId = reelTimeClient.uploadVideoToUncategorizedReel(token, 'first')
        def secondId = reelTimeClient.uploadVideoToUncategorizedReel(token, 'second')
        def thirdId = reelTimeClient.uploadVideoToUncategorizedReel(token, 'third')

        when:
        def list = reelTimeClient.listVideos(token)

        then:
        list.size() == 3

        and:
        responseChecker.assertVideoIdInList(list, firstId)
        responseChecker.assertVideoIdInList(list, secondId)
        responseChecker.assertVideoIdInList(list, thirdId)
    }

    void "multiple users upload videos to different reels"() {
        given:
        reelTimeClient.addReel(token, 'some reel')

        and:
        def otherToken = registerNewUserAndGetToken('other', ['videos-write', 'reels-write'])
        reelTimeClient.addReel(otherToken, 'other reel')

        and:
        def firstId = reelTimeClient.uploadVideoToReel(otherToken, 'other reel', 'first')
        def secondId = reelTimeClient.uploadVideoToUncategorizedReel(otherToken, 'second')
        def thirdId = reelTimeClient.uploadVideoToReel(token, 'some reel', 'third')
        def fourthId = reelTimeClient.uploadVideoToReel(otherToken, 'other reel', 'fourth')

        when:
        def list = reelTimeClient.listVideos(token)

        then:
        list.size() == 4

        and:
        responseChecker.assertVideoIdInList(list, firstId)
        responseChecker.assertVideoIdInList(list, secondId)
        responseChecker.assertVideoIdInList(list, thirdId)
        responseChecker.assertVideoIdInList(list, fourthId)
    }
}
