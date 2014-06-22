package in.reeltime.oauth2

import java.security.SecureRandom

class ClientRegistrationService {

    // A length of 42 combined with the symbol set containing 70 choices above will give us a strength of 256-bits:
    // L = H / log(N) where L = 42, H = 256 and N= 70
    private static final REQUIRED_SECRET_LENGTH = 42
    private static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@,;.-=+'

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

        while(clientIdIsNotUnique(generatedId)) {
            generatedId = UUID.randomUUID()
        }
        return generatedId
    }

    private static boolean clientIdIsNotUnique(String clientId) {
        Client.findByClientId(clientId) != null
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
