package in.reeltime.user

import grails.test.spock.IntegrationSpec
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
        Following.findByFollowerAndFollowee(follower, followee) == null
    }

    void "attempt to remove user who is not a followee"() {
        when:
        followingService.stopFollowingUser(follower, followee)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "[${follower.username}] is not following [${followee.username}]"
    }

    void "remove user from all following to which the user is the follower"() {
        given:
        def followees = []

        3.times { it ->
            def followee = UserFactory.createUser('followee' + it)
            followees << followee

            followingService.startFollowingUser(follower, followee)

            assert Following.findByFollowerAndFollowee(follower, followee) != null
        }

        assert followees.size() == 3

        when:
        followingService.removeFollowerFromAllFollowings(follower)

        then:
        Following.findAllByFollower(follower).size() == 0
    }

    void "remove user from all followings to which the user is a followee"() {
        given:
        def followers = []

        3.times { it ->
            def follower = UserFactory.createUser('follower' + it)
            followers << follower

            followingService.startFollowingUser(follower, followee)

            assert Following.findByFollowerAndFollowee(follower, followee) != null
        }

        assert followers.size() == 3

        when:
        followingService.removeFolloweeFromAllFollowings(followee)

        then:
        Following.findAllByFollowee(followee).size() == 0
    }
}
