package in.reeltime.oauth2

import grails.test.mixin.integration.Integration
import grails.test.runtime.DirtiesRuntime
import grails.transaction.Rollback
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory

@Integration
@Rollback
class ClientServiceIntegrationSpec extends Specification {

    @Autowired
    ClientService clientService

    static final int TEST_MAX_CLIENTS_PER_PAGE = 2

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

    @DirtiesRuntime
    void "user has no clients to list"() {
        given:
        changeMaxClientsPerPage(TEST_MAX_CLIENTS_PER_PAGE)

        and:
        def user = createUserWithNoClients('someone')

        expect:
        clientService.listClientsForUser(user, 1).empty
    }

    @DirtiesRuntime
    void "user has only one page of clients in alphabetical order"() {
        given:
        changeMaxClientsPerPage(TEST_MAX_CLIENTS_PER_PAGE)

        and:
        def user = createUserWithNoClients('someone')

        def client1 = clientService.createAndSaveClient('banana', 'id1', 'secret1')
        def client2 = clientService.createAndSaveClient('apple', 'id2', 'secret2')

        and:
        user.addToClients(client1)
        user.addToClients(client2)
        user.save()

        when:
        def list = clientService.listClientsForUser(user, 1)

        then:
        list.size() == 2

        list[0].clientName == 'apple'
        list[1].clientName == 'banana'
    }

    @DirtiesRuntime
    void "user has multiple pages of clients"() {
        given:
        changeMaxClientsPerPage(TEST_MAX_CLIENTS_PER_PAGE)

        and:
        def user = createUserWithNoClients('someone')

        def client1 = clientService.createAndSaveClient('strawberry', 'id1', 'secret1')
        def client2 = clientService.createAndSaveClient('apple', 'id2', 'secret2')
        def client3 = clientService.createAndSaveClient('banana', 'id3', 'secret3')

        and:
        user.addToClients(client1)
        user.addToClients(client2)
        user.addToClients(client3)
        user.save()

        when:
        def pageOne = clientService.listClientsForUser(user, 1)

        then:
        pageOne.size() == 2

        pageOne[0].clientName == 'apple'
        pageOne[1].clientName == 'banana'

        when:
        def pageTwo = clientService.listClientsForUser(user, 2)

        then:
        pageTwo.size() == 1

        pageTwo[0].clientName == 'strawberry'
    }

    void changeMaxClientsPerPage(final int max) {
        clientService.maxClientsPerPage = max
    }

    private static User createUserWithNoClients(String username) {
        def user = UserFactory.createUser(username)
        user.clients = [] as Set
        user.save(failOnError: true)
        return user
    }
}
