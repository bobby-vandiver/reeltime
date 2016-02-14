package in.reeltime.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory

@Integration
@Rollback
class AuthenticationServiceIntegrationSpec extends Specification {

    @Autowired
    AuthenticationService authenticationService

    private static final USERNAME = 'username'
    private static final PASSWORD = 'password'

    private static final CLIENT_ID = 'client'
    private static final CLIENT_SECRET = 'secret'

    void createUser() {
        UserFactory.createUser(USERNAME, PASSWORD, USERNAME, "$USERNAME@test.com", CLIENT_ID, CLIENT_SECRET)
    }

    void "get current user"() {
        given:
        createUser()

        and:
        User user = null

        when:
        SpringSecurityUtils.doWithAuth(USERNAME) {
            user = authenticationService.currentUser
        }

        then:
        user != null
    }

    void "authenticate valid user"() {
        given:
        createUser()

        expect:
        authenticationService.authenticateUser(USERNAME, PASSWORD)
    }

    void "cannot authenticate unknown user"() {
        given:
        createUser()

        expect:
        !authenticationService.authenticateUser(USERNAME + 'a', PASSWORD)
    }

    void "cannot authenticate user with incorrect password"() {
        given:
        createUser()

        expect:
        !authenticationService.authenticateUser(USERNAME, PASSWORD + 'a')
    }

    void "authenticate valid client"() {
        given:
        createUser()

        expect:
        authenticationService.authenticateClient(CLIENT_ID, CLIENT_SECRET)
    }

    void "cannot authenticate unknown client"() {
        given:
        createUser()

        expect:
        !authenticationService.authenticateClient(CLIENT_ID + 'a', CLIENT_SECRET)
    }

    void "cannot authenticate client with incorrect secret"() {
        given:
        createUser()

        expect:
        !authenticationService.authenticateClient(CLIENT_ID, CLIENT_SECRET + 'a')
    }
}
