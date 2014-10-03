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
    def accountRemovalService

    static allowedMethods = [register: 'POST', registerClient: 'POST', confirm: 'POST']

    @Secured(["permitAll"])
    def register(AccountRegistrationCommand command) {

        if(!command.hasErrors()) {
            render(status: SC_CREATED, contentType: APPLICATION_JSON) {
                accountRegistrationService.registerUserAndClient(command, request.locale)
            }
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
    }

    @Secured(["permitAll"])
    def registerClient(ClientRegistrationCommand command) {

        if(!command.hasErrors()) {
            render(status: SC_CREATED, contentType: APPLICATION_JSON) {
                accountRegistrationService.registerClientForExistingUser(command.username, command.client_name)
            }
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
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

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def removeAccount() {
        accountRemovalService.removeAccountForCurrentUser()
        render(status: SC_OK)
    }

    def handleRegistrationException(RegistrationException e) {
        exceptionErrorMessageResponse(e, 'registration.internal.error', SC_SERVICE_UNAVAILABLE)
    }

    def handleConfirmationException(ConfirmationException e) {
        exceptionErrorMessageResponse(e, 'registration.confirmation.code.error', SC_BAD_REQUEST)
    }
}
