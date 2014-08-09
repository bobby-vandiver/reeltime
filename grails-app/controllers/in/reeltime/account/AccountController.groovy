package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.ConfirmationException

import static javax.servlet.http.HttpServletResponse.*

class AccountController extends AbstractController {

    def accountRegistrationService
    def accountConfirmationService

    static allowedMethods = [register: 'POST', confirm: 'POST']

    @Secured(["permitAll"])
    def register(RegistrationCommand command) {

        if(!command.hasErrors()) {
            def result = accountRegistrationService.registerUserAndClient(command, request.locale)
            render(status: SC_CREATED, contentType: JSON_CONTENT_TYPE) {
                [client_id: result.clientId, client_secret: result.clientSecret]
            }
        }
        else {
            render(status: SC_BAD_REQUEST, contentType: JSON_CONTENT_TYPE) {
                [errors: localizedMessageService.getErrorMessages(command, request.locale)]
            }
        }
    }

    def handleRegistrationException(RegistrationException e) {
        handleErrorMessageResponse(e, 'registration.internal.error', SC_SERVICE_UNAVAILABLE)
    }

    @Secured(["#oauth2.isUser()"])
    def confirm(String code) {

        if(code) {
            accountConfirmationService.confirmAccount(code)
            render(status: SC_OK)
        }
        else {
            def message = localizedMessageService.getMessage('registration.confirmation.code.required', request.locale)
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [errors: [message]]
            }
        }
    }

    def handleConfirmationException(ConfirmationException e) {
        handleErrorMessageResponse(e, 'registration.confirmation.code.error', SC_BAD_REQUEST)
    }
}
