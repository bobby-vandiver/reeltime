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
    void "username [#username] is valid [#valid]"() {
        given:
        def user = new User(username: username)

        expect:
        user.validate(['username']) == valid

        where:
        username        |   valid
        null            |   false
        ''              |   false
        'a'             |   false
        '!a'            |   false
        '!ab'           |   false
        'w' * 14 + '!'  |   false
        'r' * 16        |   false

        'xy'            |   true
        'abcde'         |   true
        'abcdef'        |   true
        'Ab2C01faqWZ'   |   true
        'r' * 15        |   true
    }

    @Unroll
    void "email [#email] is valid [#valid]"() {
        given:
        def user = new User(email: email)

        expect:
        user.validate(['email']) == valid

        where:
        email               |   valid
        null                |   false
        ''                  |   false
        'oops'              |   false
        'foo@'              |   false
        'foo@b'             |   false
        '@coffee'           |   false
        'foo@bar.com'       |   true
        'foo@bar.baz.buzz'  |   true
    }

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
