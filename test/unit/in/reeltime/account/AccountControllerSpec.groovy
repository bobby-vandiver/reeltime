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

    void setup() {
        accountRegistrationService = Mock(AccountRegistrationService)
        accountRemovalService = Mock(AccountRemovalService)

        controller.accountRegistrationService = accountRegistrationService
        controller.accountRemovalService = accountRemovalService

        defineBeans {
            userService(UserService)
        }
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
        params.username = username
        params.password = password
        params.display_name = displayName
        params.email = email
        params.client_name = clientName

        and:
        def registrationCommandValidator = { AccountRegistrationCommand command, Locale locale ->
            assert command.username == username
            assert command.password == password
            assert command.display_name == displayName
            assert command.email == email
            assert command.client_name == clientName
            assert locale == request.locale
            return registrationResult
        }

        when:
        controller.registerAccount()

        then:
        assertStatusCodeAndContentType(response, 201)

        and:
        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        json.size() == 2

        and:
        json.client_id == clientId
        json.client_secret == clientSecret

        and:
        1 * accountRegistrationService.registerUserAndClient(*_) >> { command, locale -> registrationCommandValidator(command, locale) }
    }

    void "registration exception is thrown"() {
        given:
        params.username = 'foo'
        params.password = 'secret'
        params.display_name = 'foo bar'
        params.email = 'foo@test.com'
        params.client_name = 'something'

        when:
        controller.registerAccount()

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
