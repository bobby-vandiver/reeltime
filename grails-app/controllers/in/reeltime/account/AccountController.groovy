package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.ConfirmationException

import static javax.servlet.http.HttpServletResponse.*

class AccountController {

    def accountRegistrationService
    def accountConfirmationService

    def localizedMessageService

    static allowedMethods = [register: 'POST', confirm: 'POST']

    @Secured(["permitAll"])
    def register(RegistrationCommand command) {

        if(!command.hasErrors()) {
            def result = accountRegistrationService.registerUserAndClient(command, request.locale)
            render(status: SC_CREATED, contentType: 'application/json') {
                [client_id: result.clientId, client_secret: result.clientSecret]
            }
        }
        else {
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [errors: localizedMessageService.getErrorMessages(command, request.locale)]
            }
        }
    }

    def handleRegistrationException(RegistrationException e) {
        log.warn("Handling RegistrationException: ", e)
        def message = localizedMessageService.getMessage('registration.internal.error', request.locale)

        render(status: SC_SERVICE_UNAVAILABLE, contentType: 'application/json') {
            [errors: [message]]
        }
    }

    @Secured(["#oauth2.isUser()"])
    def confirm(String code) {

        if(code) {
            accountConfirmationService.confirmAccount(code)
            render(status: SC_OK)
        }
        else {
            def message = localizedMessageService.getMessage('registration.confirmation.code.required', request.locale)
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [errors: [message]]
            }
        }
    }

    def handleConfirmationException(ConfirmationException e) {
        log.warn("Handling ConfirmationException: ", e)
        def message = localizedMessageService.getMessage('registration.confirmation.code.error', request.locale)

        render(status: SC_BAD_REQUEST, contentType: 'application/json') {
            [errors: [message]]
        }
    }
}
