package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.UserNotFoundException

import static javax.servlet.http.HttpServletResponse.*
import static in.reeltime.common.ContentTypes.APPLICATION_JSON

class ResetPasswordController extends AbstractController {

    def resetPasswordService
    def accountRegistrationService

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

        def hasClientIdErrors = command.errors.hasFieldErrors('client_id')
        def hasClientSecretErrors = command.errors.hasFieldErrors('client_secret')

        if(!command.hasErrors()) {
            resetPasswordService.resetPassword(command.username, command.new_password, command.code)

            if(command.isRegisteredClient()) {
                render(status: SC_OK)
            }
            else {
                render(status: SC_OK, contentType: APPLICATION_JSON) {
                    marshall(accountRegistrationService.registerClientForExistingUser(command.username, command.client_name))
                }
            }
        }
        else if(command.isRegisteredClient() && (hasClientIdErrors || hasClientSecretErrors) ) {
            commandErrorMessageResponse(command, command.registeredClientIsAuthentic() ? SC_FORBIDDEN : SC_UNAUTHORIZED)
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
    }

    def handleAuthorizationException(AuthorizationException e) {
        exceptionStatusCodeOnlyResponse(e, SC_FORBIDDEN)
    }
}
