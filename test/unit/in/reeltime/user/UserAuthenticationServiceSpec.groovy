package in.reeltime.user

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.security.core.Authentication
import spock.lang.Specification

@TestFor(UserAuthenticationService)
@Mock([User])
class UserAuthenticationServiceSpec extends Specification {

    SpringSecurityService springSecurityService
    Authentication authentication

    void setup() {
        authentication = Mock(Authentication)
        springSecurityService = Mock(SpringSecurityService) {
            getAuthentication() >> authentication
        }
        service.springSecurityService = springSecurityService
    }

    void "valid user is logged in"() {
        given:
        def name = 'testUser'

        and:
        def user = new User(username: name)
        user.springSecurityService = springSecurityService
        user.save(validate: false)

        and:
        def principal = Stub(GrailsUser) {
            getUsername() >> name
        }

        when:
        def loggedInUser = service.loggedInUser

        then:
        loggedInUser.username == name

        and:
        1 * authentication.getPrincipal() >> principal
    }

    void "no authentication"() {
        when:
        def loggedInUser = service.loggedInUser

        then:
        loggedInUser == null

        and:
        1 * springSecurityService.authentication >> null
    }

    void "no principal"() {
        when:
        def loggedInUser = service.loggedInUser

        then:
        loggedInUser == null

        and:
        1 * authentication.getPrincipal() >> null
    }

    void "principal is an unknown user"() {
        given:
        def principal = Stub(GrailsUser) {
            getUsername() >> 'unknownUser'
        }

        when:
        def loggedInUser = service.loggedInUser

        then:
        loggedInUser == null

        and:
        1 * authentication.getPrincipal() >> principal
    }
}
