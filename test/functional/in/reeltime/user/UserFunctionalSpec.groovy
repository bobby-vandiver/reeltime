package in.reeltime.user

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class UserFunctionalSpec extends FunctionalSpec {

    String username
    String token

    void setup() {
        username = 'listUser'
        token = registerNewUserAndGetToken(username, 'users-read')
    }

    void "invalid http methods"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.listUsersUrl, ['post', 'put', 'delete'], token)
        responseChecker.assertInvalidHttpMethods(urlFactory.getUserUrl('bob'), ['post', 'put', 'delete'], token)
    }

    void "unknown user request"() {
        given:
        def request = requestFactory.userProfile(token, 'unknown')

        when:
        def response = get(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 404, 'Requested user was not found')
    }

    void "get user profile"() {
        given:
        registerUser('someone', 'secret', 'Cowboy Bob')

        when:
        def response = reelTimeClient.userProfile(token, 'someone')

        then:
        response.status == 200
        response.json.username == 'someone'
        response.json.display_name == 'Cowboy Bob'
        response.json.follower_count == 0
        response.json.followee_count == 0
    }

    void "user is following other users and is being followed by other users"() {
        given:
        def someoneToken = registerNewUserAndGetToken('someone', 'secret', 'Cowboy Bob', USERS_SCOPES)

        and:
        def follower1Token = registerNewUserAndGetToken('follower1', USERS_SCOPES)
        def follower2Token = registerNewUserAndGetToken('follower2', USERS_SCOPES)

        reelTimeClient.followUser(follower1Token, 'someone')
        reelTimeClient.followUser(follower2Token, 'someone')

        and:
        registerNewUserAndGetToken('followee1', USERS_SCOPES)
        reelTimeClient.followUser(someoneToken, 'followee1')

        when:
        def response = reelTimeClient.userProfile(token, 'someone')

        then:
        response.status == 200
        response.json.username == 'someone'
        response.json.display_name == 'Cowboy Bob'
        response.json.follower_count == 2
        response.json.followee_count == 1
    }

    @Unroll
    void "invalid page [#page] requested"() {
        given:
        def request = new RestRequest(url: urlFactory.listUsersUrl, token: token, queryParams: [page: page])

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

    void "list users contains the current user"() {
        when:
        def list = reelTimeClient.listUsers(token)

        then:
        responseChecker.assertUsernameInList(list, username)
    }

    void "list a few users"() {
        given:
        def names = ['max', 'john', 'bill']
        names.each { registerUser(it) }

        when:
        def list = reelTimeClient.listUsers(token)

        then:
        names.each { responseChecker.assertUsernameInList(list, it) }
    }
}
