package in.reeltime.reel

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class AudienceFunctionalSpec extends FunctionalSpec {

    String readToken
    String writeToken

    String listReelsToken

    String token

    void setup() {
        readToken = getAccessTokenWithScopeForTestUser('audiences-read')
        writeToken = getAccessTokenWithScopeForTestUser('audiences-write')

        listReelsToken = getAccessTokenWithScopeForTestUser('reels-read')

        token = getAccessTokenWithScopesForTestUser(['audiences-read', 'audiences-write'])
    }

    @Unroll
    void "invalid http method for audience resource"() {
        expect:
        assertInvalidHttpMethods(getAudienceUrl(1234), ['put'], token)
    }

    @Unroll
    void "use token to access audience via [#httpMethod] requiring write access [#useReadToken]"() {
        given:
        def tokenToUse = useReadToken ? readToken : writeToken
        def request = new RestRequest(url: getAudienceUrl(1234), token: tokenToUse)

        when:
        def response = "$httpMethod"(request)

        then:
        response.status == 403

        where:
        httpMethod  |   useReadToken
        'get'       |   false
        'post'      |   true
        'delete'    |   true
    }

    @Unroll
    void "invalid reelId when performing a [#httpMethod]"() {
        given:
        def request = new RestRequest(url: getAudienceUrl('invalid123'), token: token)

        when:
        def response = "$httpMethod"(request)

        then:
        assertSingleErrorMessageResponse(response, 400, '[reelId] is required')

        where:
        _   |   httpMethod
        _   |   'get'
        _   |   'post'
        _   |   'delete'
    }

    void "reel owner cannot be a member of the audience"() {
        given:
        def reelId = getUncategorizedReelId(listReelsToken)

        and:
        def addAudienceMemberUrl = getAudienceUrl(reelId)
        def request = new RestRequest(url: addAudienceMemberUrl, token: writeToken)

        when:
        def response = post(request)

        then:
        assertSingleErrorMessageResponse(response, 403, 'Unauthorized audience operation requested')
    }

    void "current user is not a member of the audience"() {
        given:
        def reelId = getUncategorizedReelId(listReelsToken)
        def nonTestUserWriteToken = registerNewUserAndGetToken('badMember', 'audiences-write')

        and:
        def removeAudienceMemberUrl = getAudienceUrl(reelId)
        def request = new RestRequest(url: removeAudienceMemberUrl, token: nonTestUserWriteToken)

        when:
        def response = delete(request)

        then:
        assertSingleErrorMessageResponse(response, 403, 'Unauthorized audience operation requested')
    }

    void "no audience members"() {
        given:
        def reelId = getUncategorizedReelId(listReelsToken)

        when:
        def list = listAudienceMembers(reelId, readToken)

        then:
        list.size() == 0
    }

    void "add current user as audience member then remove them when current user is not the owner of the reel"() {
        given:
        def reelId = getUncategorizedReelId(listReelsToken)
        def nonTestUserWriteToken = registerNewUserAndGetToken('goodMember', 'audiences-write')

        when:
        addAudienceMember(reelId, nonTestUserWriteToken)

        then:
        listAudienceMembers(reelId, readToken).size() == 1

        when:
        removeAudienceMember(reelId, nonTestUserWriteToken)

        then:
        listAudienceMembers(reelId, readToken).size() == 0
    }
}
