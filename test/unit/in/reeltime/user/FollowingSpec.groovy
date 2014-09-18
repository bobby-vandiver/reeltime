package in.reeltime.user

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Following)
class FollowingSpec extends Specification {

    @Unroll
    void "[#key] cannot be null"() {
        given:
        def following = new Following((key): null)

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
        def following = new Following(follower: follower, followee: follower)

        expect:
        !following.validate(['followee'])
    }
}
