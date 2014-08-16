package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.ConfirmationException
import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class AccountController extends AbstractController {

    def accountRegistrationService
    def accountConfirmationService

    static allowedMethods = [register: 'POST', confirm: 'POST']

    @Secured(["permitAll"])
    def register(AccountRegistrationCommand command) {

        if(!command.hasErrors()) {
            def result = accountRegistrationService.registerUserAndClient(command, request.locale)
            render(status: SC_CREATED, contentType: APPLICATION_JSON) {
                [client_id: result.clientId, client_secret: result.clientSecret]
            }
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
    }

    def handleRegistrationException(RegistrationException e) {
        exceptionErrorMessageResponse(e, 'registration.internal.error', SC_SERVICE_UNAVAILABLE)
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def confirm(String code) {

        if(code) {
            accountConfirmationService.confirmAccount(code)
            render(status: SC_OK)
        }
        else {
            errorMessageResponse('registration.confirmation.code.required', SC_BAD_REQUEST)
        }
    }

    def handleConfirmationException(ConfirmationException e) {
        exceptionErrorMessageResponse(e, 'registration.confirmation.code.error', SC_BAD_REQUEST)
    }
}
