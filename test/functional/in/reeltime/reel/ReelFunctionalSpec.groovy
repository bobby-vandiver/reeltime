package in.reeltime.reel

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import junit.framework.Assert
import org.codehaus.groovy.grails.web.json.JSONElement
import spock.lang.Ignore
import spock.lang.Unroll

class ReelFunctionalSpec extends FunctionalSpec {

    String readToken
    String writeToken

    String uploadVideoToken

    String token

    void setup() {
        readToken = getAccessTokenWithScopeForTestUser('reels-read')
        writeToken = getAccessTokenWithScopeForTestUser('reels-write')

        uploadVideoToken = getAccessTokenWithScopeForTestUser('videos-write')

        token = getAccessTokenWithScopesForTestUser(['reels-read', 'reels-write'])
    }

    @Unroll
    void "invalid http methods #methods for [#resource]"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.getUrlForResource(resource), methods, token)

        where:
        resource            |   methods
        'reel'              |   ['get', 'put', 'delete']
        'reel/1234'         |   ['put']
        'reel/1234/5678'    |   ['get', 'put', 'post']
    }

    @Unroll
    void "use token to access [#resource] via [#httpMethod] requiring write access [#useReadToken]"() {
        given:
        def tokenToUse = useReadToken ? readToken : writeToken
        def request = new RestRequest(url: urlFactory.getUrlForResource(resource), token: tokenToUse)

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
        def request = new RestRequest(url: urlFactory.getUrlForResource(resource), token: token)

        when:
        def response = "$httpMethod"(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[reelId] is required')

        where:
        resource                |   httpMethod
        'reel/invalid123'       |   'get'
        'reel/invalid123'       |   'post'
        'reel/invalid123'       |   'delete'
        'reel/invalid123/42'    |   'delete'
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
        'invalid123'    |   400         |   '[videoId] is required'
    }

    void "missing videoId when adding video to reel"() {
        given:
        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(readToken)

        and:
        def addVideoToReelUrl = urlFactory.getReelUrl(uncategorizedReelId)
        def request = new RestRequest(url: addVideoToReelUrl, token: writeToken)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[videoId] is required')
    }

    void "attempt to list reels for an unknown user"() {
        given:
        def request = new RestRequest(url: urlFactory.getReelsListUrl('unknown-user'), token: readToken)

        when:
        def response = get(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 404, 'Requested user was not found')
    }

    void "attempt to add another uncategorized reel"() {
        given:
        def request = new RestRequest(url: urlFactory.addReelUrl, token: writeToken, customizer: {
            name = 'Uncategorized'
        })

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, 'Requested reel name is not allowed')
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
        responseChecker.assertSingleErrorMessageResponse(response, 400, 'Requested reel name is not allowed')

        where:
        _   |   reelName
        _   |   'z'
        _   |   'bad'
        _   |   'this is a really long reel name that is invalid'
    }

    void "only reel owner can delete reel"() {
        given:
        def reelId = reelTimeClient.addReel('only owner can delete', writeToken)
        def otherUserToken = registerNewUserAndGetToken('otherUser', 'reels-write')

        and:
        def deleteReelUrl = urlFactory.getReelUrl(reelId)
        def request = new RestRequest(url: deleteReelUrl, token: otherUserToken)

        when:
        def response = delete(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 403, 'Unauthorized reel operation requested')

        cleanup:
        reelTimeClient.deleteReel(reelId, writeToken)
    }

    void "attempt to add video to reel it already belongs to"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(readToken)
        def videoId = reelTimeClient.uploadVideo(uploadVideoToken)

        and:
        def request = requestFactory.addVideoToReel(writeToken, reelId, videoId)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 403, 'Unauthorized reel operation requested')
    }

    void "list reels"() {
        given:
        def request = requestFactory.listReels(readToken, TEST_USER)

        when:
        def response = get(request)

        then:
        response.status == 200

        and:
        response.json.size() == 1
        response.json[0].name == 'Uncategorized'
        response.json[0].reelId > 0
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
        response.json.reelId > 0

        and:
        response.json.reelId != uncategorizedReelId
    }

    void "delete a reel"() {
        given:
        def reelId = reelTimeClient.addReel('reel to delete', writeToken)

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
        def listVideosUrl = urlFactory.getReelUrl(reelId)
        def request = new RestRequest(url: listVideosUrl, token: readToken)

        when:
        def response = get(request)

        then:
        response.status == 200
        response.json.size() == 0
    }

    void "add video to multiple reels"() {
        given:
        def reelId = reelTimeClient.addReel('add video test reel', writeToken)
        def videoId = reelTimeClient.uploadVideo(uploadVideoToken)

        and:
        reelTimeClient.addVideoToReel(reelId, videoId, writeToken)

        and:
        def listVideosUrl = urlFactory.getReelUrl(reelId)
        def request = new RestRequest(url: listVideosUrl, token: readToken)

        when:
        def response = get(request)

        then:
        response.status == 200
        response.json.size() == 1
        response.json[0].videoId == videoId
    }

    void "uploaded video is added to another person's reel"() {
        given:
        def otherUserToken = registerNewUserAndGetToken('someone', ['reels-read', 'reels-write'])

        and:
        def reelId = reelTimeClient.getUncategorizedReelId(otherUserToken, 'someone')
        def videoId = reelTimeClient.uploadVideo(uploadVideoToken)

        when:
        reelTimeClient.addVideoToReel(reelId, videoId, otherUserToken)

        then:
        def list = reelTimeClient.listVideosInReel(reelId, otherUserToken)
        responseChecker.assertVideoIdInList(list, videoId)
    }

    void "remove video from reel"() {
        given:
        def reelId = reelTimeClient.addReel('remove video test reel', writeToken)
        def videoId = reelTimeClient.uploadVideo(uploadVideoToken)

        and:
        reelTimeClient.addVideoToReel(reelId, videoId, writeToken)
        assert reelTimeClient.listVideosInReel(reelId, readToken).size() == 1

        and:
        def removeVideoUrl = urlFactory.getRemoveVideoFromReelUrl(reelId, videoId)
        def request = new RestRequest(url: removeVideoUrl, token: writeToken)

        when:
        def response = delete(request)

        then:
        response.status == 200

        and:
        reelTimeClient.listVideosInReel(reelId, readToken).size() == 0
    }
}
