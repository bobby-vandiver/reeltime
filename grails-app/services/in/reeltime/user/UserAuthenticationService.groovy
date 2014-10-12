package in.reeltime.user

import grails.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException

class UserAuthenticationService {

    def springSecurityService
    AuthenticationManager authenticationManager

    User getCurrentUser() {
        springSecurityService.currentUser as User
    }

    @Transactional(readOnly = true)
    boolean authenticate(String username, String password) {
        try {
            def authentication = new UsernamePasswordAuthenticationToken(username, password)
            authenticationManager.authenticate(authentication)
            return true
        }
        catch(AuthenticationException e) {
            log.warn("Failed to authenticate user [$username]: ${e.message}")
            return false
        }
    }
}
