package in.reeltime.oauth2

import java.security.SecureRandom

class ClientRegistrationService {

    private static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@,;.-=+'

    // A length of 42 combined with the symbol set containing 70 choices above will give us a strength of 256-bits:
    // L = H / log(N) where L = 42, H = 256 and N= 70
    private static final REQUIRED_SECRET_LENGTH = 42

    String generateClientId() {
        String generatedId = UUID.randomUUID()

        while(clientIdIsNotUnique(generatedId)) {
            generatedId = UUID.randomUUID()
        }
        return generatedId
    }

    private static boolean clientIdIsNotUnique(String clientId) {
        Client.findByClientId(clientId) != null
    }

    String generateClientSecret() {
        def secureRandom = new SecureRandom()
        def secret = new StringBuilder()

        REQUIRED_SECRET_LENGTH.times {
            def idx = secureRandom.nextInt(ALLOWED_CHARACTERS.size())
            secret.append(ALLOWED_CHARACTERS[idx])
        }
        return secret.toString()
    }

    Client register(String name, String id, String secret) {
        new Client(
                clientName: name,
                clientId: id,
                clientSecret: secret,
                authorities: ['ROLE_NATIVE_CLIENT'],
                authorizedGrantTypes: ['password', 'refresh_token'],
                scopes: ['view', 'upload']
        ).save()
    }
}
