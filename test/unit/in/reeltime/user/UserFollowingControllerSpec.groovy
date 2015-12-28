package in.reeltime.user

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.security.AuthenticationService
import spock.lang.Unroll
import in.reeltime.reel.Reel
import in.reeltime.reel.AudienceMember

@TestFor(UserFollowingController)
@Mock([UserFollowing, User, Reel, AudienceMember])
class UserFollowingControllerSpec extends AbstractControllerSpec {

    UserService userService
    UserFollowingService userFollowingService
    AuthenticationService authenticationService

    User follower
    User followee

    def setup() {
        userService = Mock(UserService)
        userFollowingService = Mock(UserFollowingService)
        authenticationService = Mock(AuthenticationService)

        controller.userService = userService
        controller.userFollowingService = userFollowingService
        controller.authenticationService = authenticationService

        follower = new User(username: 'follower', displayName: 'follower display')
        forceSaveUser(follower)

        followee = new User(username: 'followee', displayName: 'followee display')
        forceSaveUser(followee)
    }

    void "current user begins following a user"() {
        given:
        params.username = 'followee'

        when:
        controller.followUser()

        then:
        assertStatusCode(response, 201)

        and:
        1 * authenticationService.currentUser >> follower
        1 * userService.loadUser('followee') >> followee

        and:
        1 * userFollowingService.startFollowingUser(follower, followee)
    }

    void "current user stops following a user"() {
        given:
        params.username = 'followee'

        when:
        controller.unfollowUser()

        then:
        assertStatusCode(response, 200)

        and:
        1 * authenticationService.currentUser >> follower
        1 * userService.loadUser('followee') >> followee

        and:
        1 * userFollowingService.stopFollowingUser(follower, followee)
    }

    @Unroll
    void "user to follow cannot be found for action [#actionName]"() {
        given:
        params.username = 'nobody'

        when:
        controller."$actionName"()

        then:
        assertErrorMessageResponse(response, 404, TEST_MESSAGE)

        and:
        1 * userService.loadUser('nobody') >> { throw new UserNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('user.unknown', request.locale) >> TEST_MESSAGE

        where:
        _   |   actionName
        _   |   'followUser'
        _   |   'unfollowUser'
        _   |   'listFollowers'
        _   |   'listFollowees'
    }

    @Unroll
    void "fail to execute following method [#methodName] for action [#actionName]"() {
        given:
        params.username = 'followee'

        when:
        controller."$actionName"()

        then:
        assertStatusCode(response, 403)

        and:
        1 * authenticationService.currentUser >> follower
        1 * userService.loadUser('followee') >> followee

        and:
        1 * userFollowingService."$methodName"(follower, followee) >> { throw new AuthorizationException('TEST') }

        where:
        actionName      |   methodName
        'followUser'    |   'startFollowingUser'
        'unfollowUser'  |   'stopFollowingUser'
    }

    void "list followers for specified user"() {
        given:
        params.username = 'followee'

        when:
        controller.listFollowers()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.users.size() == 1

        and:
        json.users[0].username == follower.username

        and:
        1 * userService.loadUser('followee') >> followee
        1 * userFollowingService.listFollowersForFollowee(followee, _) >> [follower]
    }

    void "list followees for specified user"() {
        given:
        params.username = 'follower'

        when:
        controller.listFollowees()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.users.size() == 1

        and:
        json.users[0].username == followee.username

        and:
        1 * userService.loadUser('follower') >> follower
        1 * userFollowingService.listFolloweesForFollower(follower, _) >> [followee]
    }

    void "use page 1 for list followers if page param is omitted"() {
        given:
        params.username = 'followee'

        when:
        controller.listFollowers()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        1 * userService.loadUser('followee') >> followee
        1 * userFollowingService.listFollowersForFollowee(followee, 1) >> []
    }

    void "specify page for list followers"() {
        given:
        params.username = 'followee'
        params.page = 4

        when:
        controller.listFollowers()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        1 * userService.loadUser('followee') >> followee
        1 * userFollowingService.listFollowersForFollowee(followee, 4) >> []
    }

    void "use page 1 for list followees if page param is omitted"() {
        given:
        params.username = 'follower'

        when:
        controller.listFollowees()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        1 * userService.loadUser('follower') >> follower
        1 * userFollowingService.listFolloweesForFollower(follower, 1) >> []
    }

    void "specify page for list followees"() {
        given:
        params.username = 'follower'
        params.page = 4

        when:
        controller.listFollowees()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        1 * userService.loadUser('follower') >> follower
        1 * userFollowingService.listFolloweesForFollower(follower, 4) >> []
    }
}
