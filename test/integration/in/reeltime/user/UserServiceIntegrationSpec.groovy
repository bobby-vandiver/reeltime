package in.reeltime.user

import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client

class UserServiceIntegrationSpec extends IntegrationSpec {

    def userService

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
}
