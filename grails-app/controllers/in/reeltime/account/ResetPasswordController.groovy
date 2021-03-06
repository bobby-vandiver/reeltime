package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AccountCodeException
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.ResetPasswordException
import in.reeltime.user.UsernameCommand

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class ResetPasswordController extends AbstractController {

    def resetPasswordService

    def userService
    def authenticationService

    @Secured(["permitAll"])
    def sendEmail(UsernameCommand command) {
        handleCommandRequest(command) {
            try {
                def user = userService.loadUser(command.username)
                resetPasswordService.sendResetPasswordEmail(user, request.locale)
                render(status: SC_OK)
            }
            catch(AccountCodeException e) {
                exceptionErrorMessageResponse(e, 'account.password.reset.email.internal.error', SC_SERVICE_UNAVAILABLE)
            }
        }
    }

    @Secured(["permitAll"])
    def resetPassword(ResetPasswordCommand command) {
        try {
            if (command.isRegisteredClient()) {
                resetPasswordForRegisteredClient(command)
            } else {
                resetPasswordForNewClient(command)
            }
        }
        catch(ResetPasswordException e) {
            exceptionErrorMessageResponse(e, 'account.password.reset.code.invalid', SC_BAD_REQUEST)
        }
    }

    private def resetPasswordForRegisteredClient(ResetPasswordCommand command) {
        handleCommandRequest(command) {
            resetPasswordService.resetPasswordForRegisteredClient(command.username, command.new_password, command.code)
            render(status: SC_OK)
        }
    }

    private def resetPasswordForNewClient(ResetPasswordCommand command) {
        handleCommandRequest(command) {
            try {
                String username = command.username
                String newPassword = command.new_password

                String code = command.code
                String clientName = command.client_name

                doRender(status: SC_CREATED, contentType: APPLICATION_JSON) {
                    marshall(resetPasswordService.resetPasswordForNewClient(username, newPassword, code, clientName))
                }
            }
            catch(RegistrationException e) {
                exceptionErrorMessageResponse(e, 'registration.internal.error', SC_SERVICE_UNAVAILABLE)
            }
        }
    }
}
