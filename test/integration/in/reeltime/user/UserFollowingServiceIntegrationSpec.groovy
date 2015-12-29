package in.reeltime.user

import grails.test.spock.IntegrationSpec
import in.reeltime.exceptions.AuthorizationException
import spock.lang.Unroll
import test.helper.UserFactory
import test.helper.UserFollowingHelper

class UserFollowingServiceIntegrationSpec extends IntegrationSpec {
    
    def userFollowingService

    UserFollowingHelper userFollowingHelper

    User follower
    User followee

    void setup() {
        userFollowingHelper = new UserFollowingHelper(userFollowingService: userFollowingService)

        follower = UserFactory.createUser('follower')
        followee = UserFactory.createUser('followee')
    }

    void "add user to followees"() {
        when:
        def following = userFollowingService.startFollowingUser(follower, followee)

        then:
        following.follower == follower
        following.followee == followee
    }

    void "attempt to follow user multiple times"() {
        given:
        userFollowingService.startFollowingUser(follower, followee)

        when:
        userFollowingService.startFollowingUser(follower, followee)

        then:
        def e = thrown(AuthorizationException)
        e.message == "User [${follower.username}] cannot follow user [${followee.username}] multiple times"
    }

    void "attempt to add follower to followees"() {
        when:
        userFollowingService.startFollowingUser(follower, follower)

        then:
        def e = thrown(AuthorizationException)
        e.message == "Cannot add follower [${follower.username}] as a followee"
    }

    void "remove user from followees"() {
        given:
        userFollowingService.startFollowingUser(follower, followee)

        when:
        userFollowingService.stopFollowingUser(follower, followee)

        then:
        UserFollowing.findByFollowerAndFollowee(follower, followee) == null
    }

    void "attempt to remove user who is not a followee"() {
        when:
        userFollowingService.stopFollowingUser(follower, followee)

        then:
        def e = thrown(AuthorizationException)
        e.message == "[${follower.username}] is not following [${followee.username}]"
    }

    void "follower can follow followee and followee can follow follower"() {
        when:
        userFollowingService.startFollowingUser(follower, followee)
        userFollowingService.startFollowingUser(followee, follower)

        then:
        notThrown(Exception)
    }

    @Unroll
    void "list [#count] users being followed"() {
        given:
        def followees = userFollowingHelper.addFolloweesToFollower(follower, count)

        when:
        def list = userFollowingService.listAllFolloweesForFollower(follower)

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
        def followers = userFollowingHelper.addFollowersToFollowee(followee, count)

        when:
        def list = userFollowingService.listAllFollowersForFollowee(followee)

        then:
        list == followers

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   5
        _   |   10
    }

    void "list followees by invalid page"() {
        expect:
        userFollowingService.listFolloweesForFollower(follower, 42) == []
    }

    void "list followers by invalid page"() {
        expect:
        userFollowingService.listFollowersForFollowee(followee, 42) == []
    }

    void "list followees by page in alphabetical order"() {
        given:
        def savedMaxUserPerPage = userFollowingService.maxUsersPerPage
        userFollowingService.maxUsersPerPage = 2

        and:
        def joe = UserFactory.createUser('joe')
        def bob = UserFactory.createUser('bob')
        def alice = UserFactory.createUser('alice')

        and:
        userFollowingService.startFollowingUser(follower, joe)
        userFollowingService.startFollowingUser(follower, bob)
        userFollowingService.startFollowingUser(follower, alice)

        when:
        def pageOne = userFollowingService.listFolloweesForFollower(follower, 1)

        then:
        pageOne.size() == 2

        and:
        pageOne[0] == alice
        pageOne[1] == bob

        when:
        def pageTwo = userFollowingService.listFolloweesForFollower(follower, 2)

        then:
        pageTwo.size() == 1

        and:
        pageTwo[0] == joe

        cleanup:
        userFollowingService.maxUsersPerPage = savedMaxUserPerPage
    }

    void "list followers by page in alphabetical order"() {
        given:
        def savedMaxUserPerPage = userFollowingService.maxUsersPerPage
        userFollowingService.maxUsersPerPage = 2

        and:
        def joe = UserFactory.createUser('joe')
        def bob = UserFactory.createUser('bob')
        def alice = UserFactory.createUser('alice')

        and:
        userFollowingService.startFollowingUser(joe, followee)
        userFollowingService.startFollowingUser(bob, followee)
        userFollowingService.startFollowingUser(alice, followee)

        when:
        def pageOne = userFollowingService.listFollowersForFollowee(followee, 1)

        then:
        pageOne.size() == 2

        and:
        pageOne[0] == alice
        pageOne[1] == bob

        when:
        def pageTwo = userFollowingService.listFollowersForFollowee(followee, 2)

        then:
        pageTwo.size() == 1

        and:
        pageTwo[0] == joe

        cleanup:
        userFollowingService.maxUsersPerPage = savedMaxUserPerPage
    }

    void "remove user from all following to which the user is the follower"() {
        given:
        def followees = userFollowingHelper.addFolloweesToFollower(follower, 3)
        assert followees.size() == 3

        when:
        userFollowingService.removeFollowerFromAllFollowings(follower)

        then:
        UserFollowing.findAllByFollower(follower).size() == 0
    }

    void "remove user from all followings to which the user is a followee"() {
        given:
        def followers = userFollowingHelper.addFollowersToFollowee(followee, 3)
        assert followers.size() == 3

        when:
        userFollowingService.removeFolloweeFromAllFollowings(followee)

        then:
        UserFollowing.findAllByFollowee(followee).size() == 0
    }
}
