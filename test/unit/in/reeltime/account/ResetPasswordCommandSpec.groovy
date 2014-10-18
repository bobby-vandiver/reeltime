package in.reeltime.account

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.oauth2.Client
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll
import in.reeltime.security.AuthenticationService
import in.reeltime.user.UserService

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

        'code'                  |   null        |   false   |   'nullable'
        'code'                  |   ''          |   false   |   'blank'
        'code'                  |   'xy'        |   true    |   null

        'client_is_registered'  |   null        |   false   |   'nullable'
        'client_is_registered'  |   true        |   true    |   null
        'client_is_registered'  |   false       |   true    |   null
    }

    @Unroll
    void "new password [#password] is valid [#valid] -- same as User domain class"() {
        given:
        def command = new ResetPasswordCommand(new_password: password)

        expect:
        command.validate(['new_password']) == valid

        and:
        command.errors.getFieldError('new_password')?.code == code

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

    void "client_id [#clientId] is valid [#valid] when client is registered"() {
        given:
        def command = new ResetPasswordCommand(client_is_registered: true, client_id: clientId)

        and:
        stubAuthenticationService(command)
        stubUserService(command, clientId)

        expect:
        command.validate(['client_id']) == valid

        and:
        command.errors.getFieldError('client_id')?.code == code

        where:
        clientId    |   valid   |   code
        null        |   false   |   'nullable'
        ''          |   false   |   'blank'
        'xy'        |   true    |   null
    }

    @Unroll
    void "key [#key] - registered client does not belong to specified user"() {
        given:
        def command = new ResetPasswordCommand(client_is_registered: true, client_id: 'real', client_secret: 'secret')

        and:
        stubAuthenticationService(command)
        stubUserService(command, 'fake')

        expect:
        !command.validate([key])

        and:
        command.errors.getFieldError(key)?.code == 'unauthorized'

        where:
        _   |   key
        _   |   'client_id'
        _   |   'client_secret'
    }

    void "client_secret [#clientSecret] is valid [#valid] when client is registered"() {
        given:
        def command = new ResetPasswordCommand(client_is_registered: true, client_id: 'ignore', client_secret: clientSecret)

        and:
        stubAuthenticationService(command)
        stubUserService(command, 'ignore')

        expect:
        command.validate(['client_secret']) == valid

        and:
        command.errors.getFieldError('client_secret')?.code == code

        where:
        clientSecret    |   valid   |   code
        null            |   false   |   'nullable'
        ''              |   false   |   'blank'
        'xy'            |   true    |   null
    }

    private stubAuthenticationService(ResetPasswordCommand command) {
        command.authenticationService = Stub(AuthenticationService) {
            authenticateClient(_, _) >> true
        }
    }

    private stubUserService(ResetPasswordCommand command, String clientId) {
        def client = new Client(clientId: clientId)
        def user = new User(clients: [client])

        command.userService = Stub(UserService) {
            loadUser(_) >> user
        }
    }

    @Unroll
    void "client is authentic [#valid] when client is registered"() {
        given:
        def command = new ResetPasswordCommand(client_is_registered: true, client_id: 'buzz', client_secret: 'bazz')
        command.authenticationService = Stub(AuthenticationService) {
            authenticateClient('buzz', 'bazz') >> valid
        }
        stubUserService(command, 'buzz')

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
