package in.reeltime.user

import grails.test.spock.IntegrationSpec
import spock.lang.Unroll
import test.helper.UserFactory
import test.helper.UserFollowingHelper

class UserIntegrationSpec extends IntegrationSpec {

    def userFollowingService
    UserFollowingHelper userFollowingHelper

    User user

    void setup() {
        userFollowingHelper = new UserFollowingHelper(userFollowingService: userFollowingService)
        user = UserFactory.createUser('someone')
    }
    @Unroll
    void "user has [#count] followers"() {
        given:
        userFollowingHelper.addFollowersToFollowee(user, count)

        expect:
        user.numberOfFollowers == count

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
    }

    @Unroll
    void "user has [#count] followees"() {
        given:
        userFollowingHelper.addFolloweesToFollower(user, count)

        expect:
        user.numberOfFollowees == count

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
    }
}
