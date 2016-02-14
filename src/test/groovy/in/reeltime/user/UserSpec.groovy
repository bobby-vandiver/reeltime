package in.reeltime.user

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel
import in.reeltime.reel.UserReel
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(User)
@Mock([Client, Reel, UserReel])
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
        'a b'           |   false
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
    void "display name [#displayName] is valid [#valid]"() {
        given:
        def user = new User(displayName: displayName)

        expect:
        user.validate(['displayName']) == valid

        where:
        displayName             |   valid
        null                    |   false
        ''                      |   false
        ' '                     |   false
        'a'                     |   false
        ' a'                    |   false
        'a '                    |   false
        '!a'                    |   false
        '!ab'                   |   false
        'w' * 19 + '!'          |   false
        'r' * 21                |   false

        'xy'                    |   true
        'a b'                   |   true
        'abcde'                 |   true
        'abcdef'                |   true
        'Ab2C01faqWZ'           |   true
        '123  bbq taco'         |   true
        'r' * 20                |   true
        'a' + ' ' * 18 + 'b'    |   true
    }

    @Unroll
    void "leading and trailing whitespace are trimmed during data binding"() {
        given:
        def user = new User(displayName: displayName)

        expect:
        user.validate(['displayName'])

        where:
        _   |   displayName
        _   |   '  word'
        _   |   'name  '
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
        0       |   true
        1       |   true
        2       |   true
        3       |   true
    }

    @Unroll
    void "user has reel [#reelToCheck] [#truth] when reel [#reelToAdd] is the only reel"() {
        given:
        def reel = createReel(name: reelToAdd)
        def user = createUser()

        and:
        assignReelOwnership(user, [reel])

        expect:
        user.hasReel(reelToCheck) == truth

        where:
        reelToAdd   |   reelToCheck     |   truth
        'something' |   'something'     |   true
        'something' |   'nothing'       |   false
    }

    void "get reel by name when user has reel"() {
        given:
        def reel = createReel(name: 'something')
        def user = createUser()

        and:
        assignReelOwnership(user, [reel])

        expect:
        user.getReel('something') == reel
    }

    void "cannot get unknown reel by name if user does not have the reel"() {
        given:
        def user = createUser(username: 'joe')

        when:
        user.getReel('something')

        then:
        def e = thrown(ReelNotFoundException)
        e.message == "User [joe] does not have reel named [something]"
    }

    private User createUser(Map overrides = [:]) {
        def user = new User()
        user.springSecurityService = Stub(SpringSecurityService)

        overrides.each { key, value ->
            user."$key" = value
        }
        user.save(validate: false)
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

    private static Reel createReel(Map overrides = [:]) {
        def reel = new Reel()

        overrides.each { key, value ->
            reel."$key" = value
        }
        reel.save(validate: false)
    }

    private static void assignReelOwnership(User user, Collection<Reel> reels) {
        reels.each { reel ->
            new UserReel(owner: user, reel: reel).save()
        }
    }
}
