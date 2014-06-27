package in.reeltime.registration

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.exceptions.RegistrationException

import static javax.servlet.http.HttpServletResponse.*

class RegistrationController {

    def registrationService
    def localizedMessageService

    static allowedMethods = [register: 'POST']

    @Secured(["permitAll"])
    def register(RegistrationCommand command) {

        if(command.hasErrors()) {
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [errors: localizedMessageService.getErrorMessages(command, request.locale)]
            }
        }
        else {
            def result = registrationService.registerUserAndClient(command.username, command.password, command.client_name)
            render(status: SC_CREATED, contentType: 'application/json') {
                [client_id: result.clientId, client_secret: result.clientSecret]
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
}
