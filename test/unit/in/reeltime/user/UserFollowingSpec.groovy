package in.reeltime.user

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec
import spock.lang.Unroll

@TestFor(UserFollowing)
@Mock([User])
class UserFollowingSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return UserFollowing
    }

    @Override
    Class getLeftPropertyClass() {
        return User
    }

    @Override
    Class getRightPropertyClass() {
        return User
    }

    @Override
    String getLeftPropertyName() {
        return 'follower'
    }

    @Override
    String getRightPropertyName() {
        return 'followee'
    }

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
