package in.reeltime.user

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.oauth2.Client
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(User)
@Mock([Client])
class UserSpec extends Specification {

    @Unroll
    void "clients list cannot be null"() {
        given:
        def user = new User(clients: null)

        expect:
        !user.validate(['clients'])
    }

    @Unroll
    void "[#count] clients is valid [#valid]"() {
        given:
        def clients = createClients(count)
        def user = new User(clients: clients)

        expect:
        user.validate(['clients']) == valid

        where:
        count   |   valid
        0       |   false
        1       |   true
        2       |   false
        3       |   false
    }

    private Collection<Client> createClients(int count) {
        def clients = []
        count.times { clients << createClient() }
        return clients
    }

    private Client createClient() {
        def client = new Client()
        client.springSecurityService = Stub(SpringSecurityService)
        client.save(validate: false)
        return client
    }
}
