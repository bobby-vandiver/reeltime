package in.reeltime.account

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll
import in.reeltime.security.AuthenticationService

@TestMixin(GrailsUnitTestMixin)
class ResetPasswordCommandSpec extends Specification {

    @Unroll
    void "key [#key] with value [#value] is valid [#valid]"() {
        given:
        def command = new ResetPasswordCommand((key): value)

        expect:
        command.validate([key]) == valid

        and:
        command.errors.getFieldError(key)?.code == code

        where:
        key                     |   value       |   valid   |   code

        'username'              |   null        |   false   |   'nullable'
        'username'              |   ''          |   false   |   'blank'
        'username'              |   'xy'        |   true    |   null

        'new_password'          |   null        |   false   |   'nullable'
        'new_password'          |   ''          |   false   |   'blank'
        'new_password'          |   'xy'        |   true    |   null

        'code'                  |   null        |   false   |   'nullable'
        'code'                  |   ''          |   false   |   'blank'
        'code'                  |   'xy'        |   true    |   null

        'client_is_registered'  |   null        |   false   |   'nullable'
        'client_is_registered'  |   true        |   true    |   null
        'client_is_registered'  |   false       |   true    |   null
    }

    @Unroll
    void "key [#key] with value [#value] is valid [#valid] when client is registered"() {
        given:
        def command = new ResetPasswordCommand(client_is_registered: true, (key): value)
        command.authenticationService = Stub(AuthenticationService) {
            authenticateClient(_, _) >> true
        }

        expect:
        command.validate([key]) == valid

        and:
        command.errors.getFieldError(key)?.code == code

        where:
        key             |   value       |   valid   |   code

        'client_id'     |   null        |   false   |   'nullable'
        'client_id'     |   ''          |   false   |   'blank'
        'client_id'     |   'xy'        |   true    |   null

        'client_secret' |   null        |   false   |   'nullable'
        'client_secret' |   ''          |   false   |   'blank'
        'client_secret' |   'xy'        |   true    |   null
    }

    @Unroll
    void "client is authentic [#valid] when client is registered"() {
        given:
        def command = new ResetPasswordCommand(client_is_registered: true, client_id: 'buzz', client_secret: 'bazz')
        command.authenticationService = Stub(AuthenticationService) {
            authenticateClient('buzz', 'bazz') >> valid
        }

        expect:
        command.validate(['client_id']) == valid
        command.errors.getFieldError('client_id')?.code == code

        and:
        command.validate(['client_secret']) == valid
        command.errors.getFieldError('client_secret')?.code == code

        and:
        command.registeredClientIsAuthentic() == valid

        where:
        valid   |   code
        false   |   'unauthenticated'
        true    |   null
    }

    @Unroll
    void "client name [#clientName] is valid [valid] when client is not registered"() {
        given:
        def command = new ResetPasswordCommand(client_is_registered: false, client_name: clientName)

        expect:
        command.validate(['client_name']) == valid

        and:
        command.errors.getFieldError('client_name')?.code == code

        where:
        clientName  |   valid   |   code

        null        |   false   |   'nullable'
        ''          |   false   |   'blank'
        'xy'        |   true    |   null
    }

    @Unroll
    void "has registered client [#registered] is [#truth]"() {
        given:
        def command = new ResetPasswordCommand(client_is_registered: registered)

        expect:
        command.registeredClient == truth

        where:
        registered  |   truth
        null        |   false
        false       |   false
        true        |   true
    }
}
