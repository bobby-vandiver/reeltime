package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.UserNotFoundException

import static javax.servlet.http.HttpServletResponse.*
import static in.reeltime.common.ContentTypes.APPLICATION_JSON

class ResetPasswordController extends AbstractController {

    def resetPasswordService

    def userService
    def authenticationService

    static allowedMethods = [sendEmail: 'POST', resetPassword: 'POST']

    @Secured(["permitAll"])
    def sendEmail(String username) {
        handleSingleParamRequest(username, 'account.reset.password.email.username.required') {
            def user = userService.loadUser(username)
            resetPasswordService.sendResetPasswordEmail(user, request.locale)
            render(status: SC_OK)
        }
    }

    @Secured(["permitAll"])
    def resetPassword(ResetPasswordCommand command) {
        if(command.isRegisteredClient()) {
            resetPasswordForRegisteredClient(command)
        }
        else {
            resetPasswordForNewClient(command)
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
            String username = command.username
            String newPassword = command.new_password

            String code = command.code
            String clientName = command.client_name

            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(resetPasswordService.resetPasswordForNewClient(username, newPassword, code, clientName))
            }
        }
    }

    def handleAuthorizationException(AuthorizationException e) {
        exceptionStatusCodeOnlyResponse(e, SC_FORBIDDEN)
    }

    def handleRegistrationException(RegistrationException e) {
        exceptionErrorMessageResponse(e, 'registration.internal.error', SC_SERVICE_UNAVAILABLE)
    }
}
