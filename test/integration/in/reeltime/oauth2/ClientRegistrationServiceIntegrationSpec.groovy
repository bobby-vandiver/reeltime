package in.reeltime.oauth2

import grails.test.spock.IntegrationSpec

class ClientRegistrationServiceIntegrationSpec extends IntegrationSpec {

    def clientRegistrationService

    private static final UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/

    void "register new native client"() {
        when:
        def client = clientRegistrationService.register('native-client-name')

        then:
        client.id > 0
        client.clientName == 'native-client-name'

        and:
        client.clientId.matches(UUID_REGEX)
        client.clientSecret.length() > 0

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
}
