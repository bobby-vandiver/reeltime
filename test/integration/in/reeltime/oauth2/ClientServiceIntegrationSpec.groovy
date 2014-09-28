package in.reeltime.oauth2

import grails.test.spock.IntegrationSpec

class ClientServiceIntegrationSpec extends IntegrationSpec {

    def clientService

    void "create new native client"() {
        when:
        def client = clientService.createAndSaveClient('native-client-name', 'native-client-id', 'native-client-secret')

        then:
        client.id > 0

        and:
        client.clientName == 'native-client-name'
        client.clientId == 'native-client-id'

        and:
        secretIsEncrypted(client, 'native-client-secret')

        and:
        client.authorities.size() == 1
        client.authorities[0] == 'ROLE_NATIVE_CLIENT'

        and:
        client.authorizedGrantTypes.size() == 2
        client.authorizedGrantTypes.contains('password')
        client.authorizedGrantTypes.contains('refresh_token')

        and:
        assertScopes(client.scopes)
    }

    private static void secretIsEncrypted(Client client, String secret) {
        assert client.clientSecret != secret
    }

    private static void assertScopes(Collection<String> scopes) {
        assert scopes.size() == 10

        ['account', 'audiences', 'reels', 'users', 'videos'].each { resource ->
            String readScope = resource + '-read'
            String writeScope = resource + '-write'

            assert scopes.contains(readScope)
            assert scopes.contains(writeScope)
        }
    }
}
