package in.reeltime.account

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.ConfirmationException
import in.reeltime.user.User
import in.reeltime.user.UserService
import spock.lang.Unroll

@TestFor(AccountController)
@Mock([User])
class AccountControllerSpec extends AbstractControllerSpec {

    AccountRegistrationService accountRegistrationService
    AccountConfirmationService accountConfirmationService
    AccountRemovalService accountRemovalService

    void setup() {
        accountRegistrationService = Mock(AccountRegistrationService)
        accountConfirmationService = Mock(AccountConfirmationService)
        accountRemovalService = Mock(AccountRemovalService)

        controller.accountRegistrationService = accountRegistrationService
        controller.accountConfirmationService = accountConfirmationService
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
        controller.register()

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

        and:
        def message = 'this is a test'

        when:
        controller.register()

        then:
        assertErrorMessageResponse(response, 503, message)

        and:
        1 * accountRegistrationService.registerUserAndClient(_, _) >> { throw new RegistrationException('TEST') }
        1 * localizedMessageService.getMessage('registration.internal.error', request.locale) >> message
    }

    void "respond with client credentials upon successful registration of a previously unknown client"() {
        given:
        def username = 'foo'
        def password = 'secret'
        def clientName = 'something'

        and:
        def clientId = 'buzz'
        def clientSecret = 'bazz'

        and:
        def registrationResult = new RegistrationResult(clientId: clientId, clientSecret: clientSecret)

        and:
        stubAuthenticationService(true)

        and:
        params.username = username
        params.password = password
        params.client_name = clientName

        when:
        controller.registerClient()

        then:
        assertStatusCodeAndContentType(response, 201)

        and:
        def json = getJsonResponse(response) as Map
        json.size() == 2

        and:
        json.client_id == clientId
        json.client_secret == clientSecret

        and:
        1 * accountRegistrationService.registerClientForExistingUser(username, clientName) >> registrationResult
    }

    @Unroll
    void "confirmation code must be present -- cannot be [#code]"() {
        given:
        def message = 'confirmation code required'

        and:
        params.code = code

        when:
        controller.confirm()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * localizedMessageService.getMessage('registration.confirmation.code.required', request.locale) >> message

        where:
        _   |   code
        _   |   null
        _   |   ''
    }

    void "pass confirmation code to service to complete account confirmation"() {
        given:
        params.code = 'let-me-in'

        when:
        controller.confirm()

        then:
        response.status == 200
        response.contentLength == 0

        and:
        1 * accountConfirmationService.confirmAccount('let-me-in')
    }

    void "handle confirmation error"() {
        given:
        def message = 'confirmation error'

        and:
        params.code = 'uh-oh'

        when:
        controller.confirm()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * accountConfirmationService.confirmAccount(_) >> { throw new ConfirmationException('TEST') }
        1 * localizedMessageService.getMessagbe('registration.confirmation.code.error', request.locale) >> message
    }

    void "remove account"() {
        when:
        controller.removeAccount()

        then:
        assertStatusCodeOnlyResponse(response, 200)

        and:
        1 * accountRemovalService.removeAccountForCurrentUser()
    }
}
