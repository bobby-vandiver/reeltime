package in.reeltime.oauth2

import in.reeltime.exceptions.RegistrationException

class ClientService {

    def securityService

    // A length of 42 combined with the symbol set containing 70 choices above will give us a strength of 256-bits:
    // L = H / log(N) where L = 42, H = 256 and N= 70
    protected static final REQUIRED_SECRET_LENGTH = 42
    protected static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@,;.-=+'

    private static final MAX_ATTEMPTS = 5

    Client createClient(String clientName, String clientId, String clientSecret) {
        new Client(
                clientName: clientName,
                clientId: clientId,
                clientSecret: clientSecret,
                authorities: ['ROLE_NATIVE_CLIENT'],
                authorizedGrantTypes: ['password', 'refresh_token'],
                scopes: ['view', 'upload']
        ).save()
    }

    String generateClientId() {
        String generatedId = UUID.randomUUID()
        int attempt = 0

        while(clientIdIsNotUnique(generatedId)) {
            ensureAttemptIsAllowed(attempt)
            attempt++
            generatedId = UUID.randomUUID()
        }
        return generatedId
    }

    private static boolean clientIdIsNotUnique(String clientId) {
        Client.findByClientId(clientId) != null
    }

    private static void ensureAttemptIsAllowed(int attempt) {
        if(attempt >= MAX_ATTEMPTS) {
            throw new RegistrationException('Cannot generate unique client id')
        }
    }

    String generateClientSecret() {
        securityService.generateSecret(REQUIRED_SECRET_LENGTH, ALLOWED_CHARACTERS)
    }
}
