package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AccountCodeException
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ConfirmationException

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN
import static javax.servlet.http.HttpServletResponse.SC_OK
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE

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
            exceptionErrorMessageResponse(e, 'accountConfirmationEmail.internal.error', SC_SERVICE_UNAVAILABLE)
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
                throw new AuthorizationException("Account confirmation failed", e)
            }
        }
    }
}
