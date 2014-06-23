package in.reeltime.registration

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.exceptions.RegistrationException

import static javax.servlet.http.HttpServletResponse.*

class RegistrationController {

    def registrationService
    def messageSource

    static allowedMethods = [register: 'POST']

    @Secured(["permitAll"])
    def register(RegistrationCommand command) {

        if(command.hasErrors()) {
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [errors: getErrorMessages(command)]
            }
        }
        else {
            def result = registrationService.registerUserAndClient(command.username, command.password, command.client_name)
            render(status: SC_CREATED, contentType: 'application/json') {
                [client_id: result.clientId, client_secret: result.clientSecret]
            }
        }
    }

    // TODO: Put this in a service -- duplicated in VideoCreationController
    private List<String> getErrorMessages(RegistrationCommand command) {
        def locale = Locale.default
        command.errors.allErrors.collect { error ->
            messageSource.getMessage(error, locale)
        }
    }

    def handleRegistrationException(RegistrationException e) {
        log.warn("Handling RegistrationException: ", e)
        def message = getMessage('registration.internal.error')

        render(status: SC_SERVICE_UNAVAILABLE, contentType: 'application/json') {
            [errors: [message]]
        }
    }

    private String getMessage(code, args = []) {
        messageSource.getMessage(code, args as Object[], request.locale)
    }
}
