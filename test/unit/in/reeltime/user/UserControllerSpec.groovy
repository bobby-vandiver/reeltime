package in.reeltime.user

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec

@TestFor(UserController)
@Mock([UserFollowing])
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

        and:
        params.username = username

        when:
        controller.getUser()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 4

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

        when:
        controller.listUsers()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 1

        and:
        json[0].username == 'foo'
        json[0].display_name == 'bar'

        and:
        1 * userService.listUsers(3) >> [user]
    }
}
