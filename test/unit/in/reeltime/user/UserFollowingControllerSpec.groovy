package in.reeltime.user

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.message.LocalizedMessageService
import spock.lang.Unroll

@TestFor(UserFollowingController)
class UserFollowingControllerSpec extends AbstractControllerSpec {

    UserService userService
    UserFollowingService userFollowingService

    LocalizedMessageService localizedMessageService

    User follower
    User followee

    def setup() {
        userService = Mock(UserService)
        userFollowingService = Mock(UserFollowingService)
        localizedMessageService = Mock(LocalizedMessageService)

        controller.userService = userService
        controller.userFollowingService = userFollowingService
        controller.localizedMessageService = localizedMessageService

        follower = new User(username: 'follower')
        followee = new User(username: 'followee')
    }

    void "current user begins following a user"() {
        given:
        params.username = 'followee'

        when:
        controller.followUser()

        then:
        assertStatusCodeOnlyResponse(response, 201)

        and:
        1 * userService.currentUser >> follower
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
        assertStatusCodeOnlyResponse(response, 200)

        and:
        1 * userService.currentUser >> follower
        1 * userService.loadUser('followee') >> followee

        and:
        1 * userFollowingService.stopFollowingUser(follower, followee)
    }

    @Unroll
    void "username param is omitted for action [#actionName]"() {
        when:
        controller."$actionName"()

        then:
        assertErrorMessageResponse(response, 400, TEST_MESSAGE)

        and:
        1 * localizedMessageService.getMessage('following.username.required', request.locale) >> TEST_MESSAGE

        where:
        _   |   actionName
        _   |   'followUser'
        _   |   'unfollowUser'
        _   |   'listFollowers'
        _   |   'listFollowees'
    }

    @Unroll
    void "user to follow cannot be found for action [#actionName]"() {
        given:
        params.username = 'nobody'

        when:
        controller."$actionName"()

        then:
        assertErrorMessageResponse(response, 400, TEST_MESSAGE)

        and:
        1 * userService.loadUser('nobody') >> { throw new UserNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('following.unknown.username', request.locale) >> TEST_MESSAGE

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
        assertErrorMessageResponse(response, 400, TEST_MESSAGE)

        and:
        1 * userService.currentUser >> follower
        1 * userService.loadUser('followee') >> followee

        and:
        1 * userFollowingService."$methodName"(follower, followee) >> { throw new IllegalArgumentException('TEST') }
        1 * localizedMessageService.getMessage('following.invalid.request', request.locale) >> TEST_MESSAGE

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
        json.size() == 1

        and:
        json[0].username == follower.username

        and:
        1 * userService.loadUser('followee') >> followee
        1 * userFollowingService.listFollowersForFollowee(followee) >> [follower]
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
        json.size() == 1

        and:
        json[0].username == followee.username

        and:
        1 * userService.loadUser('follower') >> follower
        1 * userFollowingService.listFolloweesForFollower(follower) >> [followee]
    }
}
