package in.reeltime.reel

import in.reeltime.test.rest.RestRequest
import in.reeltime.test.spec.FunctionalSpec
import spock.lang.Unroll

class ReelFunctionalSpec extends FunctionalSpec {

    String readToken
    String writeToken

    String uploadVideoToken

    String token

    void setup() {
        readToken = getAccessTokenWithScopeForTestUser('reels-read')
        writeToken = getAccessTokenWithScopeForTestUser('reels-write')

        uploadVideoToken = getAccessTokenWithScopesForTestUser(['videos-read', 'videos-write'])

        token = getAccessTokenWithScopesForTestUser(['reels-read', 'reels-write'])
    }

    @Unroll
    void "invalid http methods #methods for [#resource]"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.getUrlForApiResource(resource), methods, token)

        where:
        resource                    |   methods
        'reels'                     |   ['put', 'delete']
        'reels/1234'                |   ['post', 'put']
        'reels/1234/videos'         |   ['put', 'delete']
        'reels/1234/videos/5678'    |   ['get', 'put', 'post']
    }

    @Unroll
    void "use token to access [#resource] via [#httpMethod] requiring write access [#useReadToken]"() {
        given:
        def tokenToUse = useReadToken ? readToken : writeToken
        def request = new RestRequest(url: urlFactory.getUrlForApiResource(resource), token: tokenToUse)

        when:
        def response = "$httpMethod"(request)

        then:
        response.status == 403

        where:
        resource        |   httpMethod  |   useReadToken
        'user/foo/123'  |   'get'       |   false
        'reel'          |   'post'      |   true
        'reel/1234'     |   'get'       |   false
        'reel/1234'     |   'post'      |   true
        'reel/1234'     |   'delete'    |   true
        'reel/1234/57'  |   'delete'    |   true
    }

    void "missing reel name when adding a reel"() {
        given:
        def request = new RestRequest(url: urlFactory.addReelUrl, token: writeToken)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[name] is required')
    }

    @Unroll
    void "invalid reelId in [#resource] when performing a [#httpMethod]"() {
        given:
        def request = new RestRequest(url: urlFactory.getUrlForApiResource(resource), token: token)

        when:
        def response = "$httpMethod"(request)

        then:
        responseChecker.assertErrorMessageInResponse(response, 400, '[reel_id] is invalid')

        where:
        resource                        |   httpMethod
        'reels/invalid123'              |   'get'
        'reels/invalid123'              |   'delete'
        'reels/invalid123/videos'       |   'get'
        'reels/invalid123/videos'       |   'post'
        'reels/invalid123/videos/42'    |   'delete'
    }

    void "invalid videoId when removing video from reel"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(readToken)

        and:
        def removeVideoUrl = urlFactory.getRemoveVideoFromReelUrl(reelId, videoId)
        def request = new RestRequest(url: removeVideoUrl, token: writeToken)

        when:
        def response = delete(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, statusCode, message)

        where:
        videoId         |   statusCode  |   message
        '12'            |   404         |   'Requested video was not found'
        'invalid123'    |   400         |   '[video_id] is invalid'
    }

    void "missing videoId when adding video to reel"() {
        given:
        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(readToken)

        and:
        def addVideoToReelUrl = urlFactory.getAddVideoToReelUrl(uncategorizedReelId)
        def request = new RestRequest(url: addVideoToReelUrl, token: writeToken)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[video_id] is required')
    }

    void "attempt to list reels for an unknown user"() {
        given:
        def request = new RestRequest(url: urlFactory.getReelsListUrl('unknown-user'), token: readToken)

        when:
        def response = get(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 404, 'Requested user was not found')
    }

    void "invalid page number for listing user reels"() {
        expect:
        responseChecker.assertInvalidPageNumbers(urlFactory.getReelsListUrl(TEST_USER), readToken)
    }

    void "invalid page number for listing videos in reel"() {
        expect:
        responseChecker.assertInvalidPageNumbers(urlFactory.getListVideosInReelUrl(1234), readToken)
    }

    void "attempt to add another uncategorized reel"() {
        given:
        def request = new RestRequest(url: urlFactory.addReelUrl, token: writeToken, customizer: {
            name = 'Uncategorized'
        })

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[name] is reserved')
    }

    @Unroll
    void "attempt to add a reel with a reel name [#reelName] of invalid length"() {
        given:
        def request = new RestRequest(url: urlFactory.addReelUrl, token: writeToken, customizer: {
            name = reelName
        })

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, message)

        where:
        reelName                                            |   message
        'z'                                                 |   '[name] must be at least 5 characters long'
        'bad'                                               |   '[name] must be at least 5 characters long'
        'this is a really long reel name that is invalid'   |   '[name] must be no more than 25 characters long'
    }

    void "only reel owner can delete reel"() {
        given:
        def reelId = reelTimeClient.addReel(writeToken, 'only owner can delete')
        def otherUserToken = registerNewUserAndGetToken('otherUser', 'reels-write')

        and:
        def deleteReelUrl = urlFactory.getReelUrl(reelId)
        def request = new RestRequest(url: deleteReelUrl, token: otherUserToken)

        when:
        def response = delete(request)

        then:
        responseChecker.assertUnauthorizedError(response)

        cleanup:
        reelTimeClient.deleteReel(writeToken, reelId)
    }

    void "attempt to add video to reel it already belongs to"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(readToken)
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(uploadVideoToken)

        and:
        def request = requestFactory.addVideoToReel(writeToken, reelId, videoId)

        when:
        def response = post(request)

        then:
        responseChecker.assertUnauthorizedError(response)
    }

    void "list reels"() {
        given:
        def request = requestFactory.listReelsForUser(readToken, TEST_USER)

        when:
        def response = get(request)

        then:
        response.status == 200

        and:
        response.json.reels.size() == 1
        response.json.reels[0].name == 'Uncategorized'
        response.json.reels[0].reel_id > 0
    }

    void "add a new reel"() {
        given:
        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(readToken)

        and:
        def request = new RestRequest(url: urlFactory.addReelUrl, token: writeToken, customizer: {
            name = 'some new reel'
        })

        when:
        def response = post(request)

        then:
        response.status == 201

        and:
        response.json.name == 'some new reel'
        response.json.reel_id > 0

        and:
        response.json.reel_id != uncategorizedReelId
    }

    void "get reel"() {
        given:
        def someReelId = reelTimeClient.addReel(writeToken, 'some reel')

        and:
        def request = requestFactory.getReel(readToken, someReelId)

        when:
        def response = get(request)

        then:
        response.status == 200

        and:
        response.json.name == 'some reel'
        response.json.reel_id == someReelId
        response.json.audience_size == 0
        response.json.video_count == 0
        response.json.owner.username == TEST_USER
    }

    void "get reel that has videos and audience members"() {
        given:
        def someReelId = reelTimeClient.addReel(writeToken, 'some reel')
        reelTimeClient.uploadVideoToReel(uploadVideoToken, 'some reel', 'some video')

        and:
        def member1Token = registerNewUserAndGetToken('member1', ALL_SCOPES)
        def member2Token = registerNewUserAndGetToken('member2', ALL_SCOPES)

        reelTimeClient.addAudienceMember(member1Token, someReelId)
        reelTimeClient.addAudienceMember(member2Token, someReelId)

        and:
        def request = requestFactory.getReel(readToken, someReelId)

        when:
        def response = get(request)

        then:
        response.status == 200

        and:
        response.json.name == 'some reel'
        response.json.reel_id == someReelId
        response.json.audience_size == 2
        response.json.video_count == 1
    }

    void "delete a reel"() {
        given:
        def reelId = reelTimeClient.addReel(writeToken, 'reel to delete')

        and:
        def deleteReelUrl = urlFactory.getReelUrl(reelId)
        def request = new RestRequest(url: deleteReelUrl, token: writeToken)

        when:
        def response = delete(request)

        then:
        response.status == 200
    }

    void "list videos in reel"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(readToken)

        and:
        def listVideosUrl = urlFactory.getListVideosInReelUrl(reelId)
        def request = new RestRequest(url: listVideosUrl, token: readToken)

        when:
        def response = get(request)

        then:
        response.status == 200
        response.json.videos.size() == 0
    }

    void "add video to multiple reels"() {
        given:
        def reelId = reelTimeClient.addReel(writeToken, 'add video test reel')
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(uploadVideoToken)

        and:
        reelTimeClient.addVideoToReel(writeToken, reelId, videoId)

        and:
        def listVideosUrl = urlFactory.getListVideosInReelUrl(reelId)
        def request = new RestRequest(url: listVideosUrl, token: readToken)

        when:
        def response = get(request)

        then:
        response.status == 200
        response.json.videos.size() == 1
        response.json.videos[0].video_id == videoId
    }

    void "uploaded video is added to another person's reel"() {
        given:
        def otherUserToken = registerNewUserAndGetToken('someone', ['reels-read', 'reels-write'])

        and:
        def reelId = reelTimeClient.getUncategorizedReelId(otherUserToken, 'someone')
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(uploadVideoToken)

        when:
        reelTimeClient.addVideoToReel(otherUserToken, reelId, videoId)

        then:
        def list = reelTimeClient.listVideosInReel(otherUserToken, reelId).videos
        responseChecker.assertVideoIdInList(list, videoId)
    }

    void "remove video from reel"() {
        given:
        def reelId = reelTimeClient.addReel(writeToken, 'remove video test reel')
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(uploadVideoToken)

        and:
        reelTimeClient.addVideoToReel(writeToken, reelId, videoId)
        assert reelTimeClient.listVideosInReel(readToken, reelId).size() == 1

        and:
        def removeVideoUrl = urlFactory.getRemoveVideoFromReelUrl(reelId, videoId)
        def request = new RestRequest(url: removeVideoUrl, token: writeToken)

        when:
        def response = delete(request)

        then:
        response.status == 200

        and:
        reelTimeClient.listVideosInReel(readToken, reelId).videos.size() == 0
    }

    void "invalid page requested"() {
        expect:
        responseChecker.assertInvalidPageNumbers(urlFactory.listReelsUrl, token)
    }

    void "multiple users with different reels"() {
        given:
        def otherToken = registerNewUserAndGetToken('other', ['reels-read', 'reels-write'])
        def someToken = registerNewUserAndGetToken('some', ['reels-read', 'reels-write'])

        and:
        def firstId = reelTimeClient.addReel(token, 'first')
        def secondId = reelTimeClient.addReel(otherToken, 'second')
        def thirdId = reelTimeClient.addReel(token, 'third')
        def fourthId = reelTimeClient.addReel(someToken, 'fourth')

        and:
        def otherUncategorizedId = reelTimeClient.getUncategorizedReelId(otherToken, 'other')
        def someUncategorizedId = reelTimeClient.getUncategorizedReelId(someToken, 'some')
        def uncategorizedId = reelTimeClient.getUncategorizedReelId(token)

        when:
        def list = reelTimeClient.listReels(readToken).reels

        then:
        list.size() == 7

        and:
        responseChecker.assertReelIdInList(list, firstId)
        responseChecker.assertReelIdInList(list, secondId)
        responseChecker.assertReelIdInList(list, thirdId)
        responseChecker.assertReelIdInList(list, fourthId)
        responseChecker.assertReelIdInList(list, otherUncategorizedId)
        responseChecker.assertReelIdInList(list, someUncategorizedId)
        responseChecker.assertReelIdInList(list, uncategorizedId)
    }
}
