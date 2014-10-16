package in.reeltime.security

import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client
import in.reeltime.user.User
import test.helper.UserFactory

class AuthenticationServiceIntegrationSpec extends IntegrationSpec {

    def authenticationService

    User user
    Client client

    private static final USERNAME = 'username'
    private static final PASSWORD = 'password'

    private static final CLIENT_ID = 'client'
    private static final CLIENT_SECRET = 'secret'

    void setup() {
        user = UserFactory.createUser(USERNAME, PASSWORD, USERNAME, "$USERNAME@test.com", CLIENT_ID, CLIENT_SECRET)
    }

    void "authenticate valid user"() {
        expect:
        authenticationService.authenticateUser(USERNAME, PASSWORD)
    }

    void "cannot authenticate unknown user"() {
        expect:
        !authenticationService.authenticateUser(USERNAME + 'a', PASSWORD)
    }

    void "cannot authenticate user with incorrect password"() {
        expect:
        !authenticationService.authenticateUser(USERNAME, PASSWORD + 'a')
    }

    void "authenticate valid client"() {
        expect:
        authenticationService.authenticateClient(CLIENT_ID, CLIENT_SECRET)
    }

    void "cannot authenticate unknown client"() {
        expect:
        !authenticationService.authenticateClient(CLIENT_ID + 'a', CLIENT_SECRET)
    }

    void "cannot authenticate client with incorrect secret"() {
        expect:
        !authenticationService.authenticateClient(CLIENT_ID, CLIENT_SECRET + 'a')
    }
}
