package in.reeltime.user

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import spock.lang.Ignore
import spock.lang.Specification

@TestFor(UserController)
class UserControllerSpec extends AbstractControllerSpec {

    UserService userService

    void setup() {
        userService = Mock(UserService)
        controller.userService = userService
    }

    @Ignore("This should be a functional test to avoid convoluted mocking")
    void "invalid page requested"() {
        given:
        params.page = 0

        when:
        controller.listUsers()

        then:
        assertErrorMessageResponse(response, 400, TEST_MESSAGE)

        and:
        1 * localizedMessageService.getMessage('pagedListCommand.page.min.notmet', request.locale) >> TEST_MESSAGE
    }

    void "use page 1 if page param is omitted"() {
        when:
        controller.listUsers()

        then:
        1 * userService.listUsers(1)
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
