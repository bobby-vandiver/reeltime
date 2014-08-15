package in.reeltime.user

import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.reel.Audience
import in.reeltime.reel.Reel

class UserServiceIntegrationSpec extends IntegrationSpec {

    def userService

    Reel reel
    Client client

    void setup() {
        client = new Client(clientName: 'test-name', clientId: 'test-id').save()
        reel = new Reel(name: Reel.UNCATEGORIZED_REEL_NAME, audience: new Audience(), videos: [])
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
        def user = userService.createAndSaveUser(username, password, email, client, reel)

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
        user.reels.contains(reel)
    }

    void "load an unknown user"() {
        when:
        userService.loadUser('unknown')

        then:
        def e = thrown(UserNotFoundException)
        e.message == 'User [unknown] not found'
    }

    void "load an existing user"() {
        given:
        def username = 'exists'
        new User(username: username, password: 'unknown', email: "$username@test.com").save(validate: false)

        when:
        def user = userService.loadUser(username)

        then:
        user.username == username
    }

    void "update user"() {
        given:
        def user = createAndSaveValidUser('foo')
        assert !User.findByUsername('foo').accountExpired

        when:
        user.accountExpired = true
        userService.storeUser(user)

        then:
        User.findByUsername('foo').accountExpired
    }

    private User createAndSaveValidUser(String username) {
        userService.createAndSaveUser(username, 'bar', "username@test.com", client, reel)
    }
}
