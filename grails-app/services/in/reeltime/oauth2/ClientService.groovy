package in.reeltime.oauth2

import in.reeltime.exceptions.RegistrationException
import in.reeltime.user.User

class ClientService {

    def securityService

    // A length of 42 combined with the symbol set containing 70 choices above will give us a strength of 256-bits:
    // L = H / log(N) where L = 42, H = 256 and N= 70
    protected static final REQUIRED_SECRET_LENGTH = 42
    protected static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@,;.-=+'

    private static final MAX_ATTEMPTS = 5

    Client createAndSaveClient(String clientName, String clientId, String clientSecret) {
        new Client(
                clientName: clientName,
                clientId: clientId,
                clientSecret: clientSecret,
                authorities: ['ROLE_NATIVE_CLIENT'],
                authorizedGrantTypes: ['password', 'refresh_token'],
                scopes: createScopesList()
        ).save()
    }

    private static Collection<String> createScopesList() {
        def scopes = []
        ['account', 'audiences', 'reels', 'users', 'videos'].each { resource ->
            scopes << "$resource-read"
            scopes << "$resource-write"
        }
        return scopes
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

    void removeClientsForUser(User user) {
        def clientsToRemove = []
        clientsToRemove.addAll(user.clients)

        clientsToRemove.each { Client client ->
            log.debug "Removing client [${client.id}] from user [${user.username}]"
            user.removeFromClients(client)

            log.debug "Deleting client [${client.id}]"
            client.delete()
        }
    }
}
