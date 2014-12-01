package in.reeltime.account

import in.reeltime.common.AbstractControllerIntegrationSpec
import in.reeltime.user.User
import test.helper.ClientFactory
import test.helper.UserFactory

class ResetPasswordControllerIntegrationSpec extends AbstractControllerIntegrationSpec {

    ResetPasswordController controller

    def resetPasswordService
    def authenticationService

    def accountCodeGenerationService

    User user
    String passwordResetCode

    private static final String USERNAME = 'user'
    private static final String NEW_PASSWORD = 'password'
    private static final String INVALID_PASSWORD = 'a'

    private static final String CLIENT_ID = 'clientId'
    private static final String CLIENT_SECRET = 'clientSecret'
    private static final String CLIENT_NAME = 'clientName'

    void setup() {
        controller = new ResetPasswordController()
        controller.resetPasswordService = resetPasswordService
        controller.authenticationService = authenticationService
        controller.request.method = 'POST'

        user = UserFactory.createUser(USERNAME, 'oldPassword', 'display', 'test@test.com', CLIENT_ID, CLIENT_SECRET)
        passwordResetCode = accountCodeGenerationService.generateResetPasswordCode(user)
    }

    void "successfully reset password for previously registered client"() {
        given:
        setupParamsForPreviouslyRegisteredClient()

        when:
        controller.resetPassword()

        then:
        assertStatusCodeOnlyResponse(controller.response, 200)

        and:
        assertResetCodeHasBeenRemoved(user)
    }

    void "unable to authenticate previously registered client"() {
        given:
        setupParamsForPreviouslyRegisteredClient(client_secret: CLIENT_SECRET + 'a')

        when:
        controller.resetPassword()

        then:
        assertResponseHasErrors(controller.response, 400)

        and:
        assertResetCodeIsAvailable(user)
    }

    void "previously registered client is not associated with the user"() {
        given:
        def newClientId = CLIENT_ID.reverse()
        def newClientSecret = CLIENT_SECRET.reverse()

        ClientFactory.createClient(newClientId, newClientSecret)
        setupParamsForPreviouslyRegisteredClient(client_id: newClientId, client_secret: newClientSecret)

        when:
        controller.resetPassword()

        then:
        assertResponseHasErrors(controller.response, 400)

        and:
        assertResetCodeIsAvailable(user)
    }

    void "param fails validation for previously registered client"() {
        given:
        setupParamsForPreviouslyRegisteredClient(new_password: INVALID_PASSWORD)

        when:
        controller.resetPassword()

        then:
        assertResponseHasErrors(controller.response, 400)

        and:
        assertResetCodeIsAvailable(user)
    }

    void "successfully reset for a new client"() {
        given:
        setupParamsForNewClient()

        when:
        controller.resetPassword()

        then:
        assertStatusCodeAndContentType(controller.response, 200)

        and:
        def json = getJsonResponse(controller.response)
        json.size() == 2

        and:
        def clientId = json.client_id
        def clientSecret = json.client_secret

        authenticationService.authenticateClient(clientId, clientSecret)
        user.clients.find { it.clientId == clientId } != null

        and:
        assertResetCodeHasBeenRemoved(user)
    }

    void "param fails validation for new client"() {
        given:
        setupParamsForNewClient(new_password: INVALID_PASSWORD)

        when:
        controller.resetPassword()

        then:
        assertResponseHasErrors(controller.response, 400)

        and:
        assertResetCodeIsAvailable(user)
    }

    private void setupParamsForPreviouslyRegisteredClient(Map overrides = [:]) {
        setupCommonParams()

        controller.params.client_is_registered = true
        controller.params.client_id = CLIENT_ID
        controller.params.client_secret = CLIENT_SECRET

        overrideParams(overrides)
    }

    private void setupParamsForNewClient(Map overrides = [:]) {
        setupCommonParams()

        controller.params.client_is_registered = false
        controller.params.client_name = CLIENT_NAME

        overrideParams(overrides)
    }

    private void setupCommonParams() {
        controller.params.username = USERNAME
        controller.params.new_password = NEW_PASSWORD
        controller.params.code = passwordResetCode
    }

    private void overrideParams(Map overrides) {
        overrides.each { key, value ->
            controller.params.remove(key)
            controller.params.put(key, value)
        }
    }
}
