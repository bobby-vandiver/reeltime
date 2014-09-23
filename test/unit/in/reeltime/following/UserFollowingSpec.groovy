package in.reeltime.following

import grails.test.mixin.TestFor
import in.reeltime.following.UserFollowing
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(UserFollowing)
class UserFollowingSpec extends Specification {

    @Unroll
    void "[#key] cannot be null"() {
        given:
        def following = new UserFollowing((key): null)

        expect:
        !following.validate([key])

        where:
        _   |   key
        _   |   'follower'
        _   |   'followee'
    }

    void "follower cannot be a followee"() {
        given:
        def follower = new User()
        def following = new UserFollowing(follower: follower, followee: follower)

        expect:
        !following.validate(['followee'])
    }
}
