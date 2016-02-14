package in.reeltime.user

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.reel.AudienceMember
import in.reeltime.reel.Reel
import in.reeltime.reel.UserReel

@TestFor(UserController)
@Mock([UserFollowing, User, Reel, AudienceMember, UserReel])
class UserControllerSpec extends AbstractControllerSpec {

    UserService userService

    void setup() {
        userService = Mock(UserService)
        controller.userService = userService
    }

    void "get user details"() {
        given:
        def username = 'foo'
        def displayName = 'bar'

        def user = new User(username: username, displayName: displayName)
        forceSaveUser(user)

        and:
        params.username = username

        when:
        controller.getUser()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)

        and:
        json.username == username
        json.display_name == displayName

        and:
        json.follower_count == 0
        json.followee_count == 0

        and:
        1 * userService.loadUser(username) >> user
    }

    void "use page 1 if page param is omitted"() {
        when:
        controller.listUsers()

        then:
        1 * userService.listUsers(1) >> []
    }

    void "list users"() {
        given:
        params.page = 3

        and:
        def user = new User(username: 'foo', displayName: 'bar')
        forceSaveUser(user)

        when:
        controller.listUsers()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.users.size() == 1

        and:
        json.users[0].username == 'foo'
        json.users[0].display_name == 'bar'

        and:
        1 * userService.listUsers(3) >> [user]
    }
}
