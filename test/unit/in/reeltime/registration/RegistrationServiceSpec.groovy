package in.reeltime.registration

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.oauth2.Client
import in.reeltime.oauth2.ClientService
import in.reeltime.user.User
import in.reeltime.user.UserService
import spock.lang.Specification

@TestFor(RegistrationService)
@Mock([User, AccountVerification])
class RegistrationServiceSpec extends Specification {

    UserService userService
    ClientService clientService

    SpringSecurityService springSecurityService

    void setup() {
        userService = Mock(UserService)
        clientService = Mock(ClientService)
        springSecurityService = Mock(SpringSecurityService)

        service.userService = userService
        service.clientService = clientService
        service.springSecurityService = springSecurityService
    }

    void "return client id and client secret in registration result"() {
        given:
        def email = 'foo@test.com'
        def username = 'foo'
        def password = 'bar'
        def clientName = 'something'

        and:
        def clientId = 'buzz'
        def clientSecret = 'bazz'

        and:
        def client = new Client()

        and:
        def command = new RegistrationCommand(username: username, password: password,
                email: email, client_name: clientName)

        when:
        def result = service.registerUserAndClient(command)

        then:
        result.clientId == clientId
        result.clientSecret == clientSecret

        and:
        1 * clientService.generateClientId() >> clientId
        1 * clientService.generateClientSecret() >> clientSecret
        1 * clientService.createClient(clientName, clientId, clientSecret) >> client

        and:
        1 * userService.createUser(username, password, email, client)
    }

    void "verify account"() {
        given:
        def user = new User(username: 'foo')
        user.springSecurityService = Stub(SpringSecurityService)
        user.save(validate: false)

        and:
        def rawCode = '1234abcd'
        def salt = 'z14aflaa'.bytes

        def hashedCode = service.hashVerificationCode(rawCode, salt)
        def accountVerification = new AccountVerification(user: user, code: hashedCode, salt: salt).save()

        and:
        def accountVerificationId = accountVerification.id

        when:
        service.verifyAccount(rawCode)

        then:
        user.verified

        and:
        !AccountVerification.findById(accountVerificationId)

        and:
        1 * springSecurityService.currentUser >> user
        1 * userService.updateUser(user)
    }
}
