package in.reeltime.user

import grails.test.mixin.TestFor
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(UserAuthenticationService)
class UserAuthenticationServiceSpec extends Specification {

    AuthenticationManager authenticationManager

    void setup() {
        authenticationManager = Mock(AuthenticationManager)
        service.authenticationManager = authenticationManager
    }

    @Unroll
    void "authenticate valid user [#valid]"() {
        given:
        def username = 'foo'
        def password = 'bar'

        when:
        def authenticated = service.authenticate(username, password)

        then:
        authenticated == valid

        and:
        1 * authenticationManager.authenticate(_) >> { Authentication auth ->
            assert auth instanceof UsernamePasswordAuthenticationToken
            assert auth.principal == username
            assert auth.credentials == password

            if(!valid) {
                throw new BadCredentialsException('TEST')
            }
        }

        where:
        _   |   valid
        _   |   true
        _   |   false
    }
}
