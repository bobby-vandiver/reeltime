package in.reeltime.reel

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import junit.framework.Assert
import spock.lang.Unroll

class ReelFunctionalSpec extends FunctionalSpec {

    String readToken
    String writeToken

    String uploadVideoToken

    String token

    void setup() {
        readToken = getAccessTokenWithScope('reels-read')
        writeToken = getAccessTokenWithScope('reels-write')

        uploadVideoToken = getAccessTokenWithScope('videos-write')

        token = getAccessTokenWithScopes(['reels-read', 'reels-write'])
    }

    @Unroll
    void "invalid http methods #methods for [#resource]"() {
        expect:
        assertInvalidHttpMethods(getUrlForResource(resource), methods, token)

        where:
        resource            |   methods
        'reel'              |   ['get', 'put', 'delete']
        'reel/1234/5678'    |   ['get', 'put', 'post']
    }

    // The 403 status code is a side effect of mapping the action
    // based on the HTTP method in UrlMappings
    void "PUT is forbidden for reel resource"() {
        given:
        def request = new RestRequest(url: getUrlForResource('reel/1234'), token: token)

        when:
        def response = restClient.put(request)

        then:
        response.status == 403
    }

    void "missing reel name when adding a reel"() {
        given:
        def request = new RestRequest(url: getUrlForResource('reel'), token: writeToken)

        when:
        def response = restClient.post(request)

        then:
        assertSingleErrorMessageResponse(response, 400, '[name] is required')
    }

    @Unroll
    void "invalid reelId in [#resource] when performing a [#httpMethod]"() {
        given:
        def request = new RestRequest(url: getUrlForResource('reel/invalid123'), token: token)

        when:
        def response = restClient."$httpMethod"(request)

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
        def reelId = uncategorizedReelId

        and:
        def removeVideoUrl = getUrlForResource("reel/$reelId/$videoId")
        def request = new RestRequest(url: removeVideoUrl, token: writeToken)

        when:
        def response = restClient.delete(request)

        then:
        assertSingleErrorMessageResponse(response, 400, message)

        where:
        videoId         |   message
        '12'            |   'Requested video was not found'
        'invalid123'    |   '[videoId] is required'
    }

    void "missing videoId when adding video to reel"() {
        given:
        def uncategorizedReelId = uncategorizedReelId

        and:
        def addVideoToReelUrl = getUrlForResource("reel/$uncategorizedReelId")
        def request = new RestRequest(url: addVideoToReelUrl, token: writeToken)

        when:
        def response = restClient.post(request)

        then:
        assertSingleErrorMessageResponse(response, 400, '[videoId] is required')
    }

    void "attempt to list reels for an unknown user"() {
        given:
        def request = new RestRequest(url: getUrlForResource('/user/unknown-user/reels'), token: readToken)

        when:
        def response = restClient.get(request)

        then:
        assertSingleErrorMessageResponse(response, 400, 'Requested user was not found')
    }

    void "attempt to add another uncategorized reel"() {
        given:
        def request = new RestRequest(url: getUrlForResource('reel'), token: writeToken, customizer: {
            name = 'Uncategorized'
        })

        when:
        def response = restClient.post(request)

        then:
        assertSingleErrorMessageResponse(response, 400, 'Requested reel name is not allowed')
    }

    void "list reels"() {
        given:
        def request = createListReelsRequest()

        when:
        def response = restClient.get(request)

        then:
        response.status == 200

        and:
        response.json.size() == 1
        response.json[0].name == 'Uncategorized'
        response.json[0].reelId > 0
    }

    void "add a new reel"() {
        given:
        def uncategorizedReelId = uncategorizedReelId

        and:
        def request = new RestRequest(url: getUrlForResource('reel'), token: writeToken, customizer: {
            name = 'some new reel'
        })

        when:
        def response = restClient.post(request)

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
        def deleteReelUrl = getUrlForResource("reel/$reelId")
        def request = new RestRequest(url: deleteReelUrl, token: writeToken)

        when:
        def response = restClient.delete(request)

        then:
        response.status == 200
    }

    private Long addReel(String reelName) {
        def request = new RestRequest(url: getUrlForResource('reel'), token: writeToken, customizer: {
            name = reelName
        })

        def response = restClient.post(request)
        if(response.status != 201) {
            Assert.fail("Failed to add reel [$reelName]. Status: ${response.status} JSON: ${response.json}")
        }
        return response.json.reelId
    }

    private void addVideoToReel(Long reelId, Long vid) {
        def request = new RestRequest(url: getUrlForResource("reel/$reelId"), token: writeToken, customizer: {
            videoId = vid
        })

        def response = restClient.post(request)
        if(response.status != 200) {
            Assert.fail("Failed to add video [$vid] to reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }

    private Long getUncategorizedReelId() {
        def request = createListReelsRequest()
        def response = restClient.get(request)

        if(response.status != 200) {
            Assert.fail("Failed to list reels. Status: ${response.status} JSON: ${response.json}")
        }
        def uncategorizedReel = response.json.find { it.name == 'Uncategorized' }
        return uncategorizedReel.reelId
    }

    private RestRequest createListReelsRequest() {
        def reelsListUrl = getUrlForResource("/user/$TEST_USER/reels")
        new RestRequest(url: reelsListUrl, token: readToken)
    }
}
