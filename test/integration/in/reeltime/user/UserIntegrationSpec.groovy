package in.reeltime.user

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.reel.AudienceMember
import spock.lang.Unroll
import test.helper.ReelFactory
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

    void "new user does not have uncategorized reel"() {
        given:
        def newUser = new User(username: 'joe')

        expect:
        !newUser.validate(['reels'])
    }

    void "removing uncategorized reel is invalid"() {
        given:
        user.reels = []

        expect:
        !user.validate(['reels'])
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

    @Unroll
    void "user has [#count] reels"() {
        given:
        final int numberOfDefaultReels = 1
        createMultipleReels(count)

        expect:
        user.numberOfReels == count + numberOfDefaultReels

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
    }

    @Unroll
    void "user is an audience member of [#count] reels"() {
        given:
        joinReels(count)

        expect:
        user.numberOfAudienceMemberships == count

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
    }

    void "current user is following this user"() {
        given:
        def currentUser = UserFactory.createUser('current')
        userFollowingService.startFollowingUser(currentUser, user)

        expect:
        SpringSecurityUtils.doWithAuth('current') {
            user.currentUserIsFollowing
        }
    }

    void "current user is not following this user"() {
        given:
        UserFactory.createUser('current')

        expect:
        SpringSecurityUtils.doWithAuth('current') {
            !user.currentUserIsFollowing
        }
    }

    private void createMultipleReels(int count) {
        count.times {
            ReelFactory.createReel(user, "test-${count}")
        }
    }

    private void joinReels(int count) {
        def anotherUser = UserFactory.createUser('another')

        count.times {
            def reel = ReelFactory.createReel(anotherUser, "test-${count}")
            new AudienceMember(reel: reel, member: user).save()
        }
    }
}
