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

    void "create a new following with no followees"() {
        when:
        def following = followingService.createFollowingForFollower(follower)

        then:
        following.follower == follower
        following.followees.size() == 0

        and:
        Following.findByFollower(follower) != null
    }

    void "delete following for follower"() {
        given:
        followingService.createFollowingForFollower(follower)

        when:
        followingService.deleteFollowingForFollower(follower)

        then:
        Following.findByFollower(follower) == null
    }

    void "deleting following does not affect the users involved"() {
        given:
        followingService.createFollowingForFollower(follower)
        followingService.startFollowingUser(follower, followee)

        when:
        followingService.deleteFollowingForFollower(follower)

        then:
        User.findById(follower.id) != null
        User.findById(followee.id) != null
    }

    void "add user to followees"() {
        given:
        def following = followingService.createFollowingForFollower(follower)

        when:
        followingService.startFollowingUser(follower, followee)

        then:
        following.followees.size() == 1
        following.followees.contains(followee)
    }

    void "attempt to add follower to followees"() {
        given:
        followingService.createFollowingForFollower(follower)

        when:
        followingService.startFollowingUser(follower, follower)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Cannot add follower [${follower.username}] as a followee"
    }

    void "remove user from followees"() {
        given:
        def following = followingService.createFollowingForFollower(follower)
        followingService.startFollowingUser(follower, followee)

        when:
        followingService.stopFollowingUser(follower, followee)

        then:
        following.followees.size() == 0
    }

    void "attempt to remove user who is not a followee"() {
        given:
        followingService.createFollowingForFollower(follower)

        when:
        followingService.stopFollowingUser(follower, followee)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "[${follower.username}] is not following [${followee.username}]"
    }
}
