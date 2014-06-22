package in.reeltime.oauth2

import in.reeltime.exceptions.RegistrationException

import java.security.SecureRandom

class ClientRegistrationService {

    // A length of 42 combined with the symbol set containing 70 choices above will give us a strength of 256-bits:
    // L = H / log(N) where L = 42, H = 256 and N= 70
    private static final REQUIRED_SECRET_LENGTH = 42
    private static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@,;.-=+'

    private static final MAX_ATTEMPTS = 5

    Client register(String name) {
        new Client(
                clientName: name,
                clientId: generateClientId(),
                clientSecret: generateClientSecret(),
                authorities: ['ROLE_NATIVE_CLIENT'],
                authorizedGrantTypes: ['password', 'refresh_token'],
                scopes: ['view', 'upload']
        ).save()
    }

    protected static String generateClientId() {
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

    protected static String generateClientSecret() {
        def secureRandom = new SecureRandom()
        def secret = new StringBuilder()

        REQUIRED_SECRET_LENGTH.times {
            def idx = secureRandom.nextInt(ALLOWED_CHARACTERS.size())
            secret.append(ALLOWED_CHARACTERS[idx])
        }
        return secret.toString()
    }
}
