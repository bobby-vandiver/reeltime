package in.reeltime.registration

import in.reeltime.exceptions.RegistrationException

import static javax.servlet.http.HttpServletResponse.*

class RegistrationController {

    def userRegistrationService
    def clientRegistrationService

    def register(String username, String password, String client_name) {

        def clientId = clientRegistrationService.generateClientId()
        def clientSecret = clientRegistrationService.generateClientSecret()
        def client = clientRegistrationService.register(client_name, clientId, clientSecret)
        userRegistrationService.register(username, password, client)

        render(status: SC_CREATED, contentType: 'application/json') {
            [client_id: clientId, client_secret: clientSecret]
        }
    }

    def handleRegistrationException(RegistrationException e) {
        render(status: SC_SERVICE_UNAVAILABLE, contentType: 'application/json') {
            [error: 'Unable to register. Please try again.']
        }
    }
}
