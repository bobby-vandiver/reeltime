package in.reeltime.registration

import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client

class ClientRegistrationServiceIntegrationSpec extends IntegrationSpec {

    def clientRegistrationService

    void "register new native client"() {
        when:
        def client = clientRegistrationService.register('native-client-name', 'native-client-id', 'native-client-secret')

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
        client.scopes.size() == 2
        client.scopes.contains('view')
        client.scopes.contains('upload')
    }

    private static void secretIsEncrypted(Client client, String secret) {
        assert client.clientSecret != secret
    }
}
