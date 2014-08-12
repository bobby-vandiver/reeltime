package in.reeltime.reel

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class ReelFunctionalSpec extends FunctionalSpec {

    String readToken
    String writeToken

    String token

    void setup() {
        readToken = getAccessTokenWithScope('reels-read')
        writeToken = getAccessTokenWithScope('reels-write')

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
}
