package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AccountCodeException
import in.reeltime.exceptions.ConfirmationException

import static javax.servlet.http.HttpServletResponse.*

class AccountConfirmationController extends AbstractController {

    def accountConfirmationService
    def authenticationService

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def sendEmail() {
        try {
            def currentUser = authenticationService.currentUser
            accountConfirmationService.sendConfirmationEmail(currentUser, request.locale)
            render(status: SC_OK)
        }
        catch(AccountCodeException e) {
            exceptionErrorMessageResponse(e, 'account.confirmation.email.internal.error', SC_SERVICE_UNAVAILABLE)
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def confirmAccount(AccountConfirmationCommand command) {
        handleCommandRequest(command) {
            try {
                accountConfirmationService.confirmAccount(command.code)
                render(status: SC_OK)
            }
            catch(ConfirmationException e) {
                exceptionErrorMessageResponse(e, 'account.confirmation.code.invalid', SC_BAD_REQUEST)
            }
        }
    }
}
