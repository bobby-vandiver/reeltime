package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.ConfirmationException

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import static javax.servlet.http.HttpServletResponse.SC_OK

class AccountConfirmationController extends AbstractController {

    def accountConfirmationService

    static allowedMethods = [confirmAccount: 'POST']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def confirmAccount(String code) {

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
