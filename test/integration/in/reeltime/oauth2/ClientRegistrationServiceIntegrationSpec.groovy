package in.reeltime.oauth2

import grails.test.spock.IntegrationSpec

class ClientRegistrationServiceIntegrationSpec extends IntegrationSpec {

    def clientRegistrationService

    private static final TEST_CLIENT_NAME = 'test-name'

    private static final TEST_CLIENT_ID = 'test-id'
    private static final TEST_CLIENT_SECRET = 'test-secret'

    void "register new native client"() {
        when:
        def client = clientRegistrationService.register(TEST_CLIENT_NAME, TEST_CLIENT_ID, TEST_CLIENT_SECRET)

        then:
        client.id > 0
        client.clientName == TEST_CLIENT_NAME

        and:
        client.clientId == TEST_CLIENT_ID
        client.clientSecret != TEST_CLIENT_SECRET

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
