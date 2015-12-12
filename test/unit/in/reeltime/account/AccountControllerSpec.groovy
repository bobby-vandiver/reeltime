package in.reeltime.account

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User
import in.reeltime.user.UserService

@TestFor(AccountController)
@Mock([User])
class AccountControllerSpec extends AbstractControllerSpec {

    AccountRegistrationService accountRegistrationService
    AccountRemovalService accountRemovalService

    UserService userService

    void setup() {
        accountRegistrationService = Mock(AccountRegistrationService)
        accountRemovalService = Mock(AccountRemovalService)

        controller.accountRegistrationService = accountRegistrationService
        controller.accountRemovalService = accountRemovalService

        userService = Stub(UserService)
    }

    void "respond with client credentials upon successful registration"() {
        given:
        def username = 'foo'
        def password = 'secret'

        def displayName = 'foo bar'

        def email = 'foo@test.com'
        def clientName = 'something'

        and:
        def clientId = 'buzz'
        def clientSecret = 'bazz'

        and:
        def registrationResult = new RegistrationResult(clientId: clientId, clientSecret: clientSecret)

        and:
        def command = new AccountRegistrationCommand(
                username: username,
                password: password,
                display_name: displayName,
                email: email,
                client_name: clientName,
                userService: userService
        )

        and:
        def registrationCommandValidator = { AccountRegistrationCommand c, Locale locale ->
            assert c.username == username
            assert c.password == password
            assert c.display_name == displayName
            assert c.email == email
            assert c.client_name == clientName
            assert locale == request.locale
            return registrationResult
        }

        when:
        controller.registerAccount(command)

        then:
        assertStatusCodeAndContentType(response, 201)

        and:
        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        json.size() == 2

        and:
        json.client_id == clientId
        json.client_secret == clientSecret

        and:
        1 * accountRegistrationService.registerUserAndClient(*_) >> { c, locale -> registrationCommandValidator(c, locale) }
    }

    void "registration exception is thrown"() {
        given:
        def command = new AccountRegistrationCommand(
                username: 'foo',
                password: 'secret',
                display_name: 'foo bar',
                email: 'foo@test.com',
                client_name: 'something',
                userService: userService
        )

        when:
        controller.registerAccount(command)

        then:
        assertErrorMessageResponse(response, 503, TEST_MESSAGE)

        and:
        1 * accountRegistrationService.registerUserAndClient(_, _) >> { throw new RegistrationException('TEST') }
        1 * localizedMessageService.getMessage('registration.internal.error', request.locale) >> TEST_MESSAGE
    }

    void "remove account"() {
        when:
        controller.removeAccount()

        then:
        assertStatusCode(response, 200)

        and:
        1 * accountRemovalService.removeAccountForCurrentUser()
    }

    void "authorization exception is thrown"() {
        when:
        controller.removeAccount()

        then:
        assertStatusCode(response, 403)

        and:
        1 * accountRemovalService.removeAccountForCurrentUser() >> { throw new AuthorizationException('TEST') }
    }
}
