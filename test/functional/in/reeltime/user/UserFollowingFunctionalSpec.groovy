package in.reeltime.user

import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class UserFollowingFunctionalSpec extends FunctionalSpec {

    String followerToken
    String followeeToken

    void setup() {
        followerToken = registerNewUserAndGetToken('follower', ['users-read', 'users-write'])
        followeeToken = registerNewUserAndGetToken('followee', ['users-read', 'users-write'])
    }

    @Unroll
    void "invalid http methods for [#urlMethod]"() {
        given:
        def url = urlFactory."$urlMethod"('someone')

        expect:
        responseChecker.assertInvalidHttpMethods(url, httpMethods)

        where:
        urlMethod               |   httpMethods
        'getFollowUrl'          |   ['get', 'put']
        'getListFollowersUrl'   |   ['post', 'put', 'delete']
        'getListFolloweesUrl'   |   ['post', 'put', 'delete']
    }

    @Unroll
    void "invalid page number [#page] for [#requestMethod]"() {
        given:
        def request = requestFactory."$requestMethod"(followerToken, 'follower', page)

        when:
        def response = get(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, message)

        where:
        requestMethod   |   page    |   message
        'listFollowers' |   0       |   '[page] must be a positive number'
        'listFollowers' |   -1      |   '[page] must be a positive number'
        'listFollowers' |   'foo'   |   '[page] is invalid'

        'listFollowees' |   0       |   '[page] must be a positive number'
        'listFollowees' |   -1      |   '[page] must be a positive number'
        'listFollowees' |   'foo'   |   '[page] is invalid'
    }

    void "begin following a user"() {
        when:
        reelTimeClient.followUser(followerToken, 'followee')

        then:
        reelTimeClient.listFollowers(followerToken, 'follower').users.size() == 0
        reelTimeClient.listFollowees(followerToken, 'followee').users.size() == 0

        and:
        def followees = reelTimeClient.listFollowees(followerToken, 'follower')
        followees.users.size() == 1

        and:
        followees.users[0].username == 'followee'
        followees.users[0].display_name == 'followee'

        and:
        def followers = reelTimeClient.listFollowers(followerToken, 'followee')
        followers.users.size() == 1

        and:
        followers.users[0].username == 'follower'
        followers.users[0].display_name == 'follower'
    }

    void "stop following a user"() {
        given:
        reelTimeClient.followUser(followerToken, 'followee')

        when:
        reelTimeClient.unfollowUser(followerToken, 'followee')

        then:
        reelTimeClient.listFollowees(followerToken, 'follower').users.size() == 0
        reelTimeClient.listFollowers(followerToken, 'followee').users.size() == 0
    }

    void "follower can follow followee and followee can follow follower"() {
        when:
        reelTimeClient.followUser(followerToken, 'followee')
        reelTimeClient.followUser(followeeToken, 'follower')

        then:
        reelTimeClient.listFollowees(followerToken, 'follower').users.size() == 1
        reelTimeClient.listFollowers(followeeToken, 'followee').users.size() == 1
    }
}
