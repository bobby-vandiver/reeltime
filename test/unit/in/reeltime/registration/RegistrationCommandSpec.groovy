package in.reeltime.registration

import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.user.UserService
import spock.lang.Specification
import in.reeltime.user.User
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@Mock([User])
class RegistrationCommandSpec extends Specification {

    @Unroll
    void "username [#username] is valid [#valid]"() {
        given:
        def command = new RegistrationCommand(username: username)
        command.userService = mockUserService(username, false)

        expect:
        command.validate(['username']) == valid

        and:
        command.errors.getFieldError('username')?.code == code

        where:
        username    |   valid   |   code
        'someone'   |   true    |   null
        'a'         |   true    |   null
        ''          |   false   |   'blank'
        null        |   false   |   'nullable'
    }

    void "username must be available"() {
        given:
        def existingUser = new User(username: 'foo', password: 'bar').save(validate: false)
        assert existingUser.id

        and:
        def command = new RegistrationCommand(username: 'foo')
        command.userService = mockUserService('foo', true)

        expect:
        !command.validate(['username'])

        and:
        command.errors.getFieldError('username').code == 'unavailable'
    }

    @Unroll
    void "password [#password] is valid [#valid]"() {
        given:
        def command = new RegistrationCommand(password: password)

        expect:
        command.validate(['password']) == valid

        and:
        command.errors.getFieldError('password')?.code == code

        where:
        password    |   valid   |   code
        'secret'    |   true    |   null
        'a'         |   true    |   null
        ''          |   false   |   'blank'
        null        |   false   |   'nullable'
    }

    @Unroll
    void "client_name [#clientName] is valid [#valid]"() {
        given:
        def command = new RegistrationCommand(client_name: clientName)

        expect:
        command.validate(['client_name']) == valid

        and:
        command.errors.getFieldError('client_name')?.code == code

        where:
        clientName  |   valid   |   code
        'name'      |   true    |   null
        'a'         |   true    |   null
        ''          |   false   |   'blank'
        null        |   false   |   'nullable'
    }

    private UserService mockUserService(String username, boolean exists) {
        def userService = Mock(UserService) {
            userExists(username) >> exists
        }
        return userService
    }
}
