package in.reeltime.user

import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.reel.Reel
import spock.lang.Unroll
import test.helper.UserFactory

class UserServiceIntegrationSpec extends IntegrationSpec {

    def userService

    Reel reel
    Client client

    static final int TEST_MAX_USERS_PER_PAGE = 3
    int savedMaxUsersPerPage

    void setup() {
        client = new Client(clientName: 'test-name', clientId: 'test-id').save()
        reel = new Reel(name: Reel.UNCATEGORIZED_REEL_NAME).save()

        savedMaxUsersPerPage = userService.maxUsersPerPage
        userService.maxUsersPerPage = TEST_MAX_USERS_PER_PAGE
    }

    void cleanup() {
        userService.maxUsersPerPage = savedMaxUsersPerPage
    }

    void "user exists"() {
        given:
        def existingUsername = 'foo'

        def existingUser = new User(
                username: existingUsername,
                password: 'unknown',
                displayName: existingUsername,
                email: "$existingUsername@test.com"
        ).save(validate: false)

        assert existingUser.id

        expect:
        userService.userExists(existingUsername)
    }

    void "user does not exist"() {
        expect:
        !userService.userExists('newUser')
    }

    void "email in use"() {
        given:
        UserFactory.createUser('someone', 'secret', 'display', 'someone@test.com')

        expect:
        userService.emailInUse('someone@test.com')
    }

    void "email not in use"() {
        expect:
        !userService.emailInUse('unknown@test.com')
    }

    void "client name not in use for user"() {
        given:
        def user = UserFactory.createUser('someone')
        def clientName = user.clients[0].clientName + 'a'

        expect:
        userService.isClientNameAvailable('someone', clientName)
    }

    void "client name is already in use for user"() {
        given:
        def user = UserFactory.createUser('someone')
        def clientName = user.clients[0].clientName

        expect:
        !userService.isClientNameAvailable('someone', clientName)
    }

    void "create new user"() {
        given:
        def email = 'foo@test.com'
        def username = 'foo'
        def password = 'bar'
        def displayName = 'foo bar'

        when:
        def user = userService.createAndSaveUser(username, password, displayName, email, client, reel)

        then:
        user.id > 0

        and:
        user.username == username
        user.displayName == displayName

        and:
        passwordIsEncrypted(user, password)

        and:
        user.clients.size() == 1
        user.clients[0] == client

        and:
        user.reels.size() == 1
        user.reels.contains(reel)
    }

    private static void passwordIsEncrypted(User user, String password) {
        assert user.password != password
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

        new User(
                username: username,
                password: 'unknown',
                displayName: username,
                email: "$username@test.com"
        ).save(validate: false)

        when:
        def user = userService.loadUser(username)

        then:
        user.username == username
    }

    void "update user"() {
        given:
        def user = UserFactory.createUser('foo')
        assert !User.findByUsername('foo').accountExpired

        when:
        user.accountExpired = true
        userService.storeUser(user)

        then:
        User.findByUsername('foo').accountExpired
    }

    @Unroll
    void "list all [#count] users"() {
        given:
        def expectedUsers = createUsers(count)

        when:
        def actualUsers = userService.listUsers(1)

        then:
        assertUsersInList(actualUsers, expectedUsers)

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
    }

    void "list users in alphabetical order"() {
        given:
        def john = UserFactory.createUser('john')
        def bob = UserFactory.createUser('bob')
        def joe = UserFactory.createUser('joe')

        when:
        def list = userService.listUsers(1)

        then:
        list.size() == 3

        and:
        list[0] == bob
        list[1] == joe
        list[2] == john
    }

    void "list users by page"() {
        given:
        def john = UserFactory.createUser('john')
        def bob = UserFactory.createUser('bob')
        def joe = UserFactory.createUser('joe')
        def mark = UserFactory.createUser('mark')

        when:
        def pageOne = userService.listUsers(1)

        then:
        pageOne.size() == 3

        and:
        pageOne[0] == bob
        pageOne[1] == joe
        pageOne[2] == john

        when:
        def pageTwo = userService.listUsers(2)

        then:
        pageTwo.size() == 1

        and:
        pageTwo[0] == mark
    }

    private Collection<User> createUsers(int count) {
        def users = []
        count.times {
            users << UserFactory.createUser('test' + it)
        }
        return users
    }

    private static void assertUsersInList(Collection<User> actualUsers, Collection<User> expectedUsers) {
        assert actualUsers.size() == expectedUsers.size()

        actualUsers.each { user ->
            assert expectedUsers.contains(user)
        }
    }
}
