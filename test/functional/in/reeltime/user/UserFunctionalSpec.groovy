package in.reeltime.user

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class UserFunctionalSpec extends FunctionalSpec {

    String token

    void setup() {
        token = registerNewUserAndGetToken('listUser', 'users-read')
    }

    void "invalid http methods"() {
        given:
        def url = urlFactory.listUsersUrl

        expect:
        responseChecker.assertInvalidHttpMethods(url, ['post', 'put', 'delete'], token)
    }

    @Unroll
    void "invalid page [#page] requested"() {
        given:
        def url = urlFactory.getListUsersUrl(page)
        def request = new RestRequest(url: url, token: token)

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
}
