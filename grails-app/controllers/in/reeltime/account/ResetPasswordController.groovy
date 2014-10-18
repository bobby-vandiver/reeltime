package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import static javax.servlet.http.HttpServletResponse.*

class ResetPasswordController extends AbstractController {

    def resetPasswordService
    def authenticationService

    static allowedMethods = [sendEmail: 'POST', resetPassword: 'POST']

    @Secured(["permitAll"])
    def sendEmail() {
        def currentUser = authenticationService.currentUser
        resetPasswordService.sendResetPasswordEmail(currentUser, request.locale)
        render(status: SC_OK)
    }

    @Secured(["permitAll"])
    def resetPassword(ResetPasswordCommand command) {

        def hasClientIdErrors = command.errors.hasFieldErrors('client_id')
        def hasClientSecretErrors = command.errors.hasFieldErrors('client_secret')

        if(!command.hasErrors()) {
            resetPasswordService.resetPassword(command.username, command.new_password, command.code)
            render(status: SC_OK)
        }
        else if(command.isRegisteredClient() && (hasClientIdErrors || hasClientSecretErrors) ) {
            render(status: command.registeredClientIsAuthentic() ? SC_FORBIDDEN : SC_UNAUTHORIZED)
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
    }
}
