package in.reeltime.reel

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import junit.framework.Assert
import org.codehaus.groovy.grails.web.json.JSONElement
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
        assertInvalidHttpMethods(getUrlForResource(resource), methods, token)

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
        def request = new RestRequest(url: getUrlForResource(resource), token: tokenToUse)

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
        def request = new RestRequest(url: addReelUrl, token: writeToken)

        when:
        def response = post(request)

        then:
        assertSingleErrorMessageResponse(response, 400, '[name] is required')
    }

    @Unroll
    void "invalid reelId in [#resource] when performing a [#httpMethod]"() {
        given:
        def request = new RestRequest(url: getUrlForResource(resource), token: token)

        when:
        def response = "$httpMethod"(request)

        then:
        assertSingleErrorMessageResponse(response, 400, '[reelId] is required')

        where:
        resource                |   httpMethod
        'reel/invalid123'       |   'get'
        'reel/invalid123'       |   'post'
        'reel/invalid123'       |   'delete'
        'reel/invalid123/42'    |   'delete'
    }

    void "invalid videoId when removing video from reel"() {
        given:
        def reelId = getUncategorizedReelId(readToken)

        and:
        def removeVideoUrl = getRemoveVideoFromReelUrl(reelId, videoId)
        def request = new RestRequest(url: removeVideoUrl, token: writeToken)

        when:
        def response = delete(request)

        then:
        assertSingleErrorMessageResponse(response, statusCode, message)

        where:
        videoId         |   statusCode  |   message
        '12'            |   404         |   'Requested video was not found'
        'invalid123'    |   400         |   '[videoId] is required'
    }

    void "missing videoId when adding video to reel"() {
        given:
        def uncategorizedReelId = getUncategorizedReelId(readToken)

        and:
        def addVideoToReelUrl = getReelUrl(uncategorizedReelId)
        def request = new RestRequest(url: addVideoToReelUrl, token: writeToken)

        when:
        def response = post(request)

        then:
        assertSingleErrorMessageResponse(response, 400, '[videoId] is required')
    }

    void "attempt to list reels for an unknown user"() {
        given:
        def request = new RestRequest(url: getReelsListUrl('unknown-user'), token: readToken)

        when:
        def response = get(request)

        then:
        assertSingleErrorMessageResponse(response, 404, 'Requested user was not found')
    }

    void "attempt to add another uncategorized reel"() {
        given:
        def request = new RestRequest(url: addReelUrl, token: writeToken, customizer: {
            name = 'Uncategorized'
        })

        when:
        def response = post(request)

        then:
        assertSingleErrorMessageResponse(response, 400, 'Requested reel name is not allowed')
    }

    @Unroll
    void "attempt to add a reel with a reel name [#reelName] of invalid length"() {
        given:
        def request = new RestRequest(url: addReelUrl, token: writeToken, customizer: {
            name = reelName
        })

        when:
        def response = post(request)

        then:
        assertSingleErrorMessageResponse(response, 400, 'Requested reel name is not allowed')

        where:
        _   |   reelName
        _   |   'z'
        _   |   'bad'
        _   |   'this is a really long reel name that is invalid'
    }

    void "only reel owner can delete reel"() {
        given:
        def reelId = addReel('only owner can delete')
        def otherUserToken = registerNewUserAndGetToken('otherUser', 'reels-write')

        and:
        def deleteReelUrl = getReelUrl(reelId)
        def request = new RestRequest(url: deleteReelUrl, token: otherUserToken)

        when:
        def response = delete(request)

        then:
        assertSingleErrorMessageResponse(response, 403, 'Unauthorized reel operation requested')

        cleanup:
        deleteReel(reelId)
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
        def uncategorizedReelId = getUncategorizedReelId(readToken)

        and:
        def request = new RestRequest(url: addReelUrl, token: writeToken, customizer: {
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
        def reelId = addReel('reel to delete')

        and:
        def deleteReelUrl = getReelUrl(reelId)
        def request = new RestRequest(url: deleteReelUrl, token: writeToken)

        when:
        def response = delete(request)

        then:
        response.status == 200
    }

    void "list videos in reel"() {
        given:
        def reelId = getUncategorizedReelId(readToken)

        and:
        def listVideosUrl = getReelUrl(reelId)
        def request = new RestRequest(url: listVideosUrl, token: readToken)

        when:
        def response = get(request)

        then:
        response.status == 200
        response.json.size() == 0
    }

    void "add video to reel"() {
        given:
        def reelId = addReel('add video test reel')
        def videoId = uploadVideo(uploadVideoToken)

        and:
        addVideoToReel(reelId, videoId)

        and:
        def listVideosUrl = getReelUrl(reelId)
        def request = new RestRequest(url: listVideosUrl, token: readToken)

        when:
        def response = get(request)

        then:
        response.status == 200
        response.json.size() == 1
        response.json[0].videoId == videoId
    }

    void "remove video from reel"() {
        given:
        def reelId = addReel('remove video test reel')
        def videoId = uploadVideo(uploadVideoToken)

        and:
        addVideoToReel(reelId, videoId)
        assert listVideosInReel(reelId).size() == 1

        and:
        def removeVideoUrl = getRemoveVideoFromReelUrl(reelId, videoId)
        def request = new RestRequest(url: removeVideoUrl, token: writeToken)

        when:
        def response = delete(request)

        then:
        response.status == 200

        and:
        listVideosInReel(reelId).size() == 0
    }

    private JSONElement listVideosInReel(Long reelId) {
        def listVideosUrl = getReelUrl(reelId)
        def request = new RestRequest(url: listVideosUrl, token: readToken)

        def response = get(request)
        if(response.status != 200) {
            Assert.fail("Failed to list videos in reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
        return response.json
    }

    private Long addReel(String reelName) {
        def request = new RestRequest(url: addReelUrl, token: writeToken, customizer: {
            name = reelName
        })

        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to add reel [$reelName]. Status: ${response.status} JSON: ${response.json}")
        }
        return response.json.reelId
    }

    private void addVideoToReel(Long reelId, Long vid) {
        def request = new RestRequest(url: getReelUrl(reelId), token: writeToken, customizer: {
            videoId = vid
        })

        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to add video [$vid] to reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }

    private void deleteReel(Long reelId) {
        def deleteReelUrl = getReelUrl(reelId)
        def request = new RestRequest(url: deleteReelUrl, token: writeToken)

        def response = delete(request)
        if(response.status != 200) {
            Assert.fail("Failed to delete reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }
}
