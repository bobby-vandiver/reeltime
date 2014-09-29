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

    void "begin following a user"() {
        when:
        reelTimeClient.followUser(followerToken, 'followee')

        then:
        reelTimeClient.listFollowers(followerToken, 'follower').size() == 0
        reelTimeClient.listFollowees(followerToken, 'followee').size() == 0

        and:
        def followees = reelTimeClient.listFollowees(followerToken, 'follower')
        followees.size() == 1

        and:
        followees[0].username == 'followee'

        and:
        def followers = reelTimeClient.listFollowers(followerToken, 'followee')
        followers.size() == 1

        and:
        followers[0].username == 'follower'
    }

    void "stop following a user"() {
        given:
        reelTimeClient.followUser(followerToken, 'followee')

        when:
        reelTimeClient.unfollowUser(followerToken, 'followee')

        then:
        reelTimeClient.listFollowees(followerToken, 'follower').size() == 0
        reelTimeClient.listFollowers(followerToken, 'followee').size() == 0
    }
}
