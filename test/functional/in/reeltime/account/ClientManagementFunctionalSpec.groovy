package in.reeltime.account

import helper.oauth2.AccessTokenRequest
import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Ignore
import spock.lang.Unroll

class ClientManagementFunctionalSpec extends FunctionalSpec {

    String token

    String clientId
    String clientSecret

    private static final USERNAME = 'client'

    void setup() {
        token = registerNewUserAndGetToken(USERNAME, ['account-read', 'account-write'])

        def clientCredentials = getClientCredentialsForRegisteredUser(USERNAME)
        clientId = clientCredentials.clientId
        clientSecret = clientCredentials.clientSecret
    }

    void "invalid http methods for url method"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.clientsUrl, ['put', 'delete'], token)
        responseChecker.assertInvalidHttpMethods(urlFactory.getRevokeClientUrl(clientId), ['get', 'post', 'put'], token)
    }

    void "register client with bad credentials"() {
        given:
        def request = new RestRequest(url: urlFactory.registerClientUrl, customizer: {
            username = 'user'
            password = 'pass'
            client_name = 'client'
        })

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() == 1
        response.json.errors[0] == 'Invalid credentials'
    }

    @Unroll
    void "register client with invalid params username [#user], password [#pass], client_name [#client]"() {
        given:
        def request = new RestRequest(url: urlFactory.registerClientUrl, customizer: {
            username = user
            password = pass
            client_name = client
        })

        when:
        def response = post(request)

        then:
        response.status == 400
        response.json.errors.size() > 0
        response.json.errors.contains(message)

        where:
        user     | pass     | client     | message
        'user'   | 'secret' | ''         | '[client_name] is required'
        'user'   | 'secret' | null       | '[client_name] is required'

        ''       | 'secret' | 'client'   | '[username] is required'
        null     | 'secret' | 'client'   | '[username] is required'

        'user'   | ''       | 'client'   | '[password] is required'
        'user'   | null     | 'client'   | '[password] is required'
    }

    void "register client name already in use for user"() {
        given:
        def request = new RestRequest(url: urlFactory.registerClientUrl, customizer: {
            username = TEST_USER
            password = TEST_PASSWORD
            client_name = 'some new client'
        })

        when:
        def response = post(request)

        then:
        response.status == 201

        when:
        response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, '[client_name] is not available')
    }

    void "register a new client for an existing user"() {
        given:
        def request = new RestRequest(url: urlFactory.registerClientUrl, customizer: {
            username = TEST_USER
            password = TEST_PASSWORD
            client_name = 'some new client'
        })

        when:
        def response = post(request)

        then:
        response.status == 201
        response.json.client_id
        response.json.client_secret

        and:
        def clientId = response.json.client_id
        def clientSecret = response.json.client_secret

        and:
        def tokenRequestForNewClient = new AccessTokenRequest(
                clientId: clientId,
                clientSecret: clientSecret,
                username: TEST_USER,
                password: TEST_PASSWORD,
                grantType: 'password',
                scope: [
                        'account-read', 'account-write',
                        'audiences-read', 'audiences-write',
                        'reels-read', 'reels-write',
                        'videos-read', 'videos-write'
                ]
        )

        and:
        def tokenRequestForExistingClient = new AccessTokenRequest(
                clientId: testClientId,
                clientSecret: testClientSecret,
                username: TEST_USER,
                password: TEST_PASSWORD,
                grantType: 'password',
                scope: [
                        'account-read', 'account-write',
                        'audiences-read', 'audiences-write',
                        'reels-read', 'reels-write',
                        'videos-read', 'videos-write'
                ]
        )

        when:
        def tokenForNewClient = getAccessTokenWithScope(tokenRequestForNewClient)
        def tokenForExistingClient = getAccessTokenWithScope(tokenRequestForExistingClient)

        then:
        tokenForNewClient != null
        tokenForExistingClient != null

        and:
        tokenForNewClient != tokenForExistingClient
    }

    void "attempt to revoke client access for unknown client"() {
        given:
        def request = requestFactory.revokeClient(token, clientId + 'a')

        when:
        def response = delete(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 404, 'Requested client was not found')
    }

    void "revoke client access for known client"() {
        given:
        def newClientId = reelTimeClient.registerNewClient(USERNAME, TEST_PASSWORD, 'revoke-test-client').client_id

        and:
        def revokeRequest = requestFactory.revokeClient(token, newClientId)

        when:
        def revokeResponse = delete(revokeRequest)

        then:
        responseChecker.assertStatusCode(revokeResponse, 200)

        when:
        def clientsList = reelTimeClient.listClients(token)

        then:
        clientsList.clients.size() == 1
        clientsList.clients[0].client_id != newClientId
    }

    void "invalid clients page"() {
        expect:
        responseChecker.assertInvalidPageNumbers(urlFactory.listClientsUrl, token)
    }

    void "list initial client"() {
        when:
        def list = reelTimeClient.listClients(token)

        then:
        list.clients.size() == 1
        list.clients[0].client_id == clientId
        list.clients[0].client_name == TEST_CLIENT_NAME
    }

    void "list multiple clients"() {
        given:
        def newClientId = reelTimeClient.registerNewClient(USERNAME, TEST_PASSWORD, 'multiple-clients').client_id

        when:
        def list = reelTimeClient.listClients(token)

        then:
        list.clients.size() == 2

        list.clients.find { it.client_id == clientId && it.client_name == TEST_CLIENT_NAME }
        list.clients.find { it.client_id == newClientId && it.client_name == 'multiple-clients' }
    }
}
