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
        _   |   'followees'
    }

    @Unroll
    void "allow [#count] followees"() {
        given:
        def followees = []
        count.times { followees << new User() }

        and:
        def following = new Following(followees: followees)

        expect:
        following.validate(['followees'])

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
    }

    void "follower cannot be a followee"() {
        given:
        def follower = new User()
        def following = new Following(follower: follower, followees: [follower])

        expect:
        !following.validate(['followees'])
    }
}
