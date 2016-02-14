package in.reeltime.reel

import in.reeltime.test.rest.RestRequest
import in.reeltime.test.spec.FunctionalSpec
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
        responseChecker.assertInvalidHttpMethods(urlFactory.getAudienceUrl(1234), ['put'], token)
    }

    void "invalid page number for list audience members"() {
        expect:
        responseChecker.assertInvalidPageNumbers(urlFactory.getAudienceUrl(1234), token)
    }

    @Unroll
    void "use token to access audience via [#httpMethod] requiring write access [#useReadToken]"() {
        given:
        def tokenToUse = useReadToken ? readToken : writeToken
        def request = new RestRequest(url: urlFactory.getAudienceUrl(1234), token: tokenToUse)

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
        def request = new RestRequest(url: urlFactory.getAudienceUrl('invalid123'), token: token)

        when:
        def response = "$httpMethod"(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[reel_id] is invalid')

        where:
        _   |   httpMethod
        _   |   'get'
        _   |   'post'
        _   |   'delete'
    }

    void "reel owner cannot be a member of the audience"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(listReelsToken)

        and:
        def addAudienceMemberUrl = urlFactory.getAudienceUrl(reelId)
        def request = new RestRequest(url: addAudienceMemberUrl, token: writeToken)

        when:
        def response = post(request)

        then:
        responseChecker.assertUnauthorizedError(response)
    }

    void "current user is not a member of the audience"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(listReelsToken)
        def nonTestUserWriteToken = registerNewUserAndGetToken('badMember', ['audiences-write', 'reels-read'])

        and:
        def removeAudienceMemberUrl = urlFactory.getAudienceUrl(reelId)
        def request = new RestRequest(url: removeAudienceMemberUrl, token: nonTestUserWriteToken)

        when:
        def response = delete(request)

        then:
        responseChecker.assertUnauthorizedError(response)

        and:
        def reel = reelTimeClient.getReel(nonTestUserWriteToken, reelId)
        reel.current_user_is_an_audience_member == false
    }

    void "current user is a member of the audience"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(listReelsToken)
        def nonTestUserWriteToken = registerNewUserAndGetToken('goodMember', ['audiences-write', 'reels-read'])

        when:
        reelTimeClient.addAudienceMember(nonTestUserWriteToken, reelId)

        then:
        def reel = reelTimeClient.getReel(nonTestUserWriteToken, reelId)
        reel.current_user_is_an_audience_member == true
    }

    void "no audience members"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(listReelsToken)

        when:
        def list = reelTimeClient.listAudienceMembers(readToken, reelId).users

        then:
        list.size() == 0
    }

    void "add current user as audience member then remove them when current user is not the owner of the reel"() {
        given:
        def reelId = reelTimeClient.getUncategorizedReelId(listReelsToken)
        def nonTestUserWriteToken = registerNewUserAndGetToken('goodMember', 'audiences-write')

        when:
        reelTimeClient.addAudienceMember(nonTestUserWriteToken, reelId)

        then:
        reelTimeClient.listAudienceMembers(readToken, reelId).users.size() == 1

        when:
        reelTimeClient.removeAudienceMember(nonTestUserWriteToken, reelId)

        then:
        reelTimeClient.listAudienceMembers(readToken, reelId).users.size() == 0
    }
}
