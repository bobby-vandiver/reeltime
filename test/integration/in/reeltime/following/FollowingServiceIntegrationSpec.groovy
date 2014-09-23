package in.reeltime.following

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import spock.lang.Unroll
import test.helper.UserFactory

class FollowingServiceIntegrationSpec extends IntegrationSpec {
    
    def followingService

    User follower
    User followee

    void setup() {
        follower = UserFactory.createUser('follower')
        followee = UserFactory.createUser('followee')
    }

    void "add user to followees"() {
        when:
        def following = followingService.startFollowingUser(follower, followee)

        then:
        following.follower == follower
        following.followee == followee
    }

    void "attempt to add follower to followees"() {
        when:
        followingService.startFollowingUser(follower, follower)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Cannot add follower [${follower.username}] as a followee"
    }

    void "remove user from followees"() {
        given:
        followingService.startFollowingUser(follower, followee)

        when:
        followingService.stopFollowingUser(follower, followee)

        then:
        UserFollowing.findByFollowerAndFollowee(follower, followee) == null
    }

    void "attempt to remove user who is not a followee"() {
        when:
        followingService.stopFollowingUser(follower, followee)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "[${follower.username}] is not following [${followee.username}]"
    }

    @Unroll
    void "list [#count] users being followed"() {
        given:
        def followees = addFolloweesToFollower(count)

        when:
        def list = followingService.listFolloweesForFollower(follower)

        then:
        list == followees

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   5
        _   |   10
    }

    @Unroll
    void "list [#count] users who are following"() {
        given:
        def followers = addFollowersToFollowee(count)

        when:
        def list = followingService.listFollowersForFollowee(followee)

        then:
        list == followers

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   5
        _   |   10
    }

    void "remove user from all following to which the user is the follower"() {
        given:
        def followees = addFolloweesToFollower(3)
        assert followees.size() == 3

        when:
        followingService.removeFollowerFromAllFollowings(follower)

        then:
        UserFollowing.findAllByFollower(follower).size() == 0
    }

    void "remove user from all followings to which the user is a followee"() {
        given:
        def followers = addFollowersToFollowee(3)
        assert followers.size() == 3

        when:
        followingService.removeFolloweeFromAllFollowings(followee)

        then:
        UserFollowing.findAllByFollowee(followee).size() == 0
    }

    private List<User> addFolloweesToFollower(int count) {
        def followees = []

        count.times { it ->
            def followee = UserFactory.createUser('followee' + it)
            followees << followee

            followingService.startFollowingUser(follower, followee)

            assert UserFollowing.findByFollowerAndFollowee(follower, followee) != null
        }

        return followees
    }

    private List<User> addFollowersToFollowee(int count) {
        def followers = []

        count.times { it ->
            def follower = UserFactory.createUser('follower' + it)
            followers << follower

            followingService.startFollowingUser(follower, followee)

            assert UserFollowing.findByFollowerAndFollowee(follower, followee) != null
        }

        return followers
    }
}
