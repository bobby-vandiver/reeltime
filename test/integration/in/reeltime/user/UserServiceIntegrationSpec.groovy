package in.reeltime.user

import grails.test.spock.IntegrationSpec

class UserServiceIntegrationSpec extends IntegrationSpec {

    def userService

    void "user exists"() {
        given:
        def existingUsername = 'foo'
        def existingUser = new User(username: existingUsername, password: 'unknown').save(validate: false)
        assert existingUser.id

        expect:
        userService.userExists(existingUsername)
    }

    void "user does not exist"() {
        expect:
        !userService.userExists('newUser')
    }
}
