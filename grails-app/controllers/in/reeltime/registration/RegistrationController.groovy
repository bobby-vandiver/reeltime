package in.reeltime.registration

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.exceptions.RegistrationException
import in.reeltime.exceptions.VerificationException

import static javax.servlet.http.HttpServletResponse.*

class RegistrationController {

    def registrationService
    def localizedMessageService

    static allowedMethods = [register: 'POST', verify: 'POST']

    @Secured(["permitAll"])
    def register(RegistrationCommand command) {

        if(!command.hasErrors()) {
            def result = registrationService.registerUserAndClient(command)
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
    def verify(String code) {

        if(code) {
            registrationService.verifyAccount(code)
            render(status: SC_OK)
        }
        else {
            def message = localizedMessageService.getMessage('registration.verification.code.required', request.locale)
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [errors: [message]]
            }
        }
    }

    def handleVerificationException(VerificationException e) {
        log.warn("Handling VerificationException: ", e)
        def message = localizedMessageService.getMessage('registration.verification.code.error', request.locale)

        render(status: SC_BAD_REQUEST, contentType: 'application/json') {
            [errors: [message]]
        }
    }
}
