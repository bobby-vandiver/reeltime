package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.RegistrationException

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class AccountController extends AbstractController {

    def accountRegistrationService
    def accountRemovalService

    static allowedMethods = [registerAccount: 'POST', removeAccount: 'DELETE']

    @Secured(["permitAll"])
    def registerAccount(AccountRegistrationCommand command) {
        handleCommandRequest(command) {
            try {
                render(status: SC_CREATED, contentType: APPLICATION_JSON) {
                    marshall(accountRegistrationService.registerUserAndClient(command, request.locale))
                }
            }
            catch(RegistrationException e) {
                exceptionErrorMessageResponse(e, 'registration.internal.error', SC_SERVICE_UNAVAILABLE)
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def removeAccount() {
        accountRemovalService.removeAccountForCurrentUser()
        render(status: SC_OK)
    }
}
