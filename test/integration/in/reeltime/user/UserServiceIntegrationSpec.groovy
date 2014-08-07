package in.reeltime.user

import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel
import spock.lang.Unroll

class UserServiceIntegrationSpec extends IntegrationSpec {

    def userService
    def reelService

    Client client

    void setup() {
        client = new Client(clientName: 'test-name', clientId: 'test-id').save()
    }

    void "user exists"() {
        given:
        def existingUsername = 'foo'
        def existingUser = new User(username: existingUsername, password: 'unknown', email: "$existingUsername@test.com").save(validate: false)
        assert existingUser.id

        expect:
        userService.userExists(existingUsername)
    }

    void "user does not exist"() {
        expect:
        !userService.userExists('newUser')
    }

    void "create new user"() {
        given:
        def email = 'foo@test.com'
        def username = 'foo'
        def password = 'bar'

        when:
        def user = userService.createAndSaveUser(username, password, email, client)

        then:
        user.id > 0

        and:
        user.username == username
        user.password != password

        and:
        user.clients.size() == 1
        user.clients[0] == client

        and:
        user.reels.size() == 1
        user.reels[0].name == 'Uncategorized'
    }

    void "update user"() {
        given:
        def user = userService.createAndSaveUser('foo', 'bar', 'foo@test.com', client)
        assert !User.findByUsername('foo').accountExpired

        when:
        user.accountExpired = true
        userService.updateUser(user)

        then:
        User.findByUsername('foo').accountExpired
    }

    @Unroll
    void "list all reels belonging to specified user -- user has [#count] reels total"() {
        given:
        def owner = userService.createAndSaveUser('foo', 'bar', 'foo@test.com', client)
        def reels = createReels(owner, count)

        when:
        def list = userService.listReels(owner.username)

        then:
        assertListsContainSameElements(list, reels)

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
        _   |   10
        _   |   100
    }

    private Collection<Reel> createReels(User owner, int count) {
        def reels = owner.reels
        def initialCount = reels.size()

        for(int i = initialCount; i < count; i++) {
            def reel = reelService.createReel(owner, "reel $i")
            reels << reel
            owner.addToReels(reel)
        }
        owner.save()
        return reels
    }

    private static void assertListsContainSameElements(Collection<?> actual, Collection<?> expected) {
        assert actual.size() == expected.size()

        expected.each { element ->
            assert actual.contains(element)
        }
    }
}
