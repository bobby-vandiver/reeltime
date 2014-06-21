package in.reeltime.oauth2

import grails.test.spock.IntegrationSpec

class ClientRegistrationServiceIntegrationSpec extends IntegrationSpec {

    def clientRegistrationService

    private static final TEST_CLIENT_NAME = 'test-name'

    private static final TEST_CLIENT_ID = 'test-id'
    private static final TEST_CLIENT_SECRET = 'test-secret'

    void "register new client"() {
        when:
        def client = clientRegistrationService.register(TEST_CLIENT_NAME, TEST_CLIENT_ID, TEST_CLIENT_SECRET)

        then:
        client.id > 0
        client.clientName == TEST_CLIENT_NAME
        client.clientId == TEST_CLIENT_ID
        client.clientSecret != TEST_CLIENT_SECRET
        client.scopes.containsAll(['view', 'upload'])
    }
}
