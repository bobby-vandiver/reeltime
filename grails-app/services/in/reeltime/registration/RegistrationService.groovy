package in.reeltime.registration

class RegistrationService {

    def userRegistrationService
    def clientRegistrationService

    RegistrationResult registerUserAndClient(String username, String password, String clientName) {
        def clientId = clientRegistrationService.generateClientId()
        def clientSecret = clientRegistrationService.generateClientSecret()

        def client = clientRegistrationService.register(clientName, clientId, clientSecret)
        userRegistrationService.register(username, password, client)

        new RegistrationResult(clientId: clientId, clientSecret: clientSecret)
    }
}
