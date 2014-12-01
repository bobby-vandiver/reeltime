package in.reeltime.account

import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.user.User
import in.reeltime.user.UserService
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@Mock([User])
class AccountRegistrationCommandSpec extends Specification {

    @Unroll
    void "username [#username] is valid [#valid] -- inherited from User domain class"() {
        given:
        def command = new AccountRegistrationCommand(username: username)
        command.userService = mockUserService(username, false)

        expect:
        command.validate(['username']) == valid

        and:
        command.errors.getFieldError('username')?.code == code

        where:
        username        |   valid   |   code
        null            |   false   |   'nullable'
        ''              |   false   |   'blank'
        'a'             |   false   |   'matches.invalid'
        '!a'            |   false   |   'matches.invalid'
        '!ab'           |   false   |   'matches.invalid'
        'w' * 14 + '!'  |   false   |   'matches.invalid'
        'r' * 16        |   false   |   'matches.invalid'
        'xy'            |   true    |   null
        'abcde'         |   true    |   null
        'abcdef'        |   true    |   null
        'someone'       |   true    |   null
        'Ab2C01faqWZ'   |   true    |   null
        'r' * 15        |   true    |   null
    }

    void "username must be available"() {
        given:
        def existingUser = new User(username: 'foo', password: 'bar').save(validate: false)
        assert existingUser.id

        and:
        def command = new AccountRegistrationCommand(username: 'foo')
        command.userService = mockUserService('foo', true)

        expect:
        !command.validate(['username'])

        and:
        command.errors.getFieldError('username').code == 'unavailable'
    }

    @Unroll
    void "display name [#displayName] is valid [#valid] -- inherited from User domain class"() {
        given:
        def command = new AccountRegistrationCommand(display_name: displayName)

        expect:
        command.validate(['display_name']) == valid

        and:
        command.errors.getFieldError('display_name')?.code == code

        where:
        displayName             |   valid   |   code
        null                    |   false   |   'nullable'
        ''                      |   false   |   'blank'
        ' '                     |   false   |   'blank'
        'a'                     |   false   |   'matches.invalid'
        ' a'                    |   false   |   'matches.invalid'
        'a '                    |   false   |   'matches.invalid'
        '!a'                    |   false   |   'matches.invalid'
        '!ab'                   |   false   |   'matches.invalid'
        'w' * 19 + '!'          |   false   |   'matches.invalid'
        'r' * 21                |   false   |   'matches.invalid'

        'xy'                    |   true    |   null
        'a b'                   |   true    |   null
        'abcde'                 |   true    |   null
        'abcdef'                |   true    |   null
        'Ab2C01faqWZ'           |   true    |   null
        '123  bbq taco'         |   true    |   null
        'r' * 20                |   true    |   null
        'a' + ' ' * 18 + 'b'    |   true    |   null
    }

    @Unroll
    void "leading and trailing whitespace are NOT trimmed during data binding -- differs from User domain class"() {
        given:
        def command = new AccountRegistrationCommand(display_name: displayName)

        expect:
        !command.validate(['display_name'])

        and:
        command.errors.getFieldError('display_name')?.code == 'matches.invalid'

        where:
        _   |   displayName
        _   |   '  word'
        _   |   'name  '
    }

    @Unroll
    void "password [#password] is valid [#valid] -- inherited from User domain class"() {
        given:
        def command = new AccountRegistrationCommand(password: password)

        expect:
        command.validate(['password']) == valid

        and:
        command.errors.getFieldError('password')?.code == code

        where:
        password                    |   valid   |   code
        null                        |   false   |   'nullable'
        ''                          |   false   |   'blank'
        'a'                         |   false   |   'minSize.notmet'
        'short'                     |   false   |   'minSize.notmet'
        'abcdef'                    |   true    |   null
        '!4ad#A'                    |   true    |   null
        'ABCAf1304z'                |   true    |   null
        '!#@$%^&*()-_=+][\\|<>/?'   |   true    |   null
    }

    @Unroll
    void "email [#email] is valid [#valid]"() {
        given:
        def command = new AccountRegistrationCommand(email: email)

        expect:
        command.validate(['email']) == valid

        and:
        command.errors.getFieldError('email')?.code == code

        where:
        email               |   valid   |   code
        null                |   false   |   'nullable'
        ''                  |   false   |   'blank'
        'oops'              |   false   |   'email.invalid'
        'foo@'              |   false   |   'email.invalid'
        'foo@b'             |   false   |   'email.invalid'
        '@coffee'           |   false   |   'email.invalid'
        'foo@bar.com'       |   true    |   null
        'foo@bar.baz.buzz'  |   true    |   null
    }

    @Unroll
    void "client_name [#clientName] is valid [#valid]"() {
        given:
        def command = new AccountRegistrationCommand(client_name: clientName)

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
