package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import static javax.servlet.http.HttpServletResponse.*

class ResetPasswordController extends AbstractController {

    def resetPasswordService
    def authenticationService

    static allowedMethods = [sendEmail: 'POST']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def sendEmail() {
        def currentUser = authenticationService.currentUser
        resetPasswordService.sendResetPasswordEmail(currentUser, request.locale)
        render(status: SC_OK)
    }
}
