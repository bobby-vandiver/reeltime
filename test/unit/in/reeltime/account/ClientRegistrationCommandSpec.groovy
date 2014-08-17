package in.reeltime.account

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.user.UserAuthenticationService
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class ClientRegistrationCommandSpec extends Specification {

    @Unroll
    void "username and password are authentic [#valid]"() {
        given:
        def command = new ClientRegistrationCommand(username: 'foo', password: 'bar')
        command.userAuthenticationService = Mock(UserAuthenticationService) {
            authenticate('foo', 'bar') >> valid
        }

        expect:
        command.validate(['username']) == valid

        and:
        command.errors.getFieldError('username')?.code == code

        where:
        valid   |   code
        false   |   'unauthenticated'
        true    |   null
    }

    @Unroll
    void "username [#username] is valid [#valid]"() {
        given:
        def command = new ClientRegistrationCommand(username: username)
        command.userAuthenticationService = Stub(UserAuthenticationService) {
            authenticate(_, _) >> true
        }

        expect:
        command.validate(['username']) == valid

        and:
        command.errors.getFieldError('username')?.code == code

        where:
        username        |   valid   |   code
        null            |   false   |   'nullable'
        ''              |   false   |   'blank'
        'xy'            |   true    |   null
        'a' * 130       |   true    |   null
    }

    @Unroll
    void "password [#password] is valid [#valid]"() {
        given:
        def command = new ClientRegistrationCommand(password: password)

        expect:
        command.validate(['password']) == valid

        and:
        command.errors.getFieldError('password')?.code == code

        where:
        password        |   valid   |   code
        null            |   false   |   'nullable'
        ''              |   false   |   'blank'
        'xy'            |   true    |   null
        'a' * 130       |   true    |   null
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
}