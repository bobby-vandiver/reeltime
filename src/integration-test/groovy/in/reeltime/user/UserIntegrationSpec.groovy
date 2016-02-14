package in.reeltime.user

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.reel.AudienceMember
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll
import in.reeltime.test.factory.ReelFactory
import in.reeltime.test.factory.UserFactory
import in.reeltime.test.util.UserFollowingHelper

@Integration
@Rollback
class UserIntegrationSpec extends Specification {

    @Autowired
    UserFollowingService userFollowingService

    UserFollowingHelper userFollowingHelper

    User user

    void setup() {
        userFollowingHelper = new UserFollowingHelper(userFollowingService: userFollowingService)
    }

    @Unroll
    void "user has [#count] followers"() {
        given:
        createUser()

        and:
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
        createUser()

        and:
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
        createUser()

        and:
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
        createUser()

        and:
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
        createUser()

        and:
        def currentUser = UserFactory.createUser('current')
        userFollowingService.startFollowingUser(currentUser, user)

        expect:
        SpringSecurityUtils.doWithAuth('current') {
            user.currentUserIsFollowing
        }
    }

    void "current user is not following this user"() {
        given:
        createUser()

        and:
        UserFactory.createUser('current')

        expect:
        SpringSecurityUtils.doWithAuth('current') {
            !user.currentUserIsFollowing
        }
    }

    private void createUser() {
        user = UserFactory.createUser('someone')
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
