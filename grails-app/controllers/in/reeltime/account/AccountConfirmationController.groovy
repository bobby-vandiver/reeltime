package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.ConfirmationException

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN
import static javax.servlet.http.HttpServletResponse.SC_OK

class AccountConfirmationController extends AbstractController {

    def accountConfirmationService
    def authenticationService

    static allowedMethods = [sendEmail: 'POST', confirmAccount: 'POST']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def sendEmail() {
        def currentUser = authenticationService.currentUser
        accountConfirmationService.sendConfirmationEmail(currentUser, request.locale)
        render(status: SC_OK)
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def confirmAccount(AccountConfirmationCommand command) {
        handleCommandRequest(command) {
            try {
                accountConfirmationService.confirmAccount(command.code)
                render(status: SC_OK)
            }
            catch(ConfirmationException e) {
                exceptionStatusCodeOnlyResponse(e, SC_FORBIDDEN)
            }
        }
    }
}
