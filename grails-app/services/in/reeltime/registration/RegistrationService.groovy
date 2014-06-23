package in.reeltime.registration

class RegistrationService {

    def userService
    def clientService

    RegistrationResult registerUserAndClient(String username, String password, String clientName) {
        def clientId = clientService.generateClientId()
        def clientSecret = clientService.generateClientSecret()

        def client = clientService.createClient(clientName, clientId, clientSecret)
        userService.createUser(username, password, client)

        new RegistrationResult(clientId: clientId, clientSecret: clientSecret)
    }
}
