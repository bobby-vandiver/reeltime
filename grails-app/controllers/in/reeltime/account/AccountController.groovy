package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.ConfirmationException
import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class AccountController extends AbstractController {

    def accountRegistrationService
    def accountRemovalService

    static allowedMethods = [register: 'POST', registerClient: 'POST']

    @Secured(["permitAll"])
    def register(AccountRegistrationCommand command) {

        if(!command.hasErrors()) {
            render(status: SC_CREATED, contentType: APPLICATION_JSON) {
                marshall(accountRegistrationService.registerUserAndClient(command, request.locale))
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
                marshall(accountRegistrationService.registerClientForExistingUser(command.username, command.client_name))
            }
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
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
}
