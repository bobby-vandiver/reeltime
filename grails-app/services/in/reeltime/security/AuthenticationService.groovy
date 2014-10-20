package in.reeltime.security

import grails.transaction.Transactional
import in.reeltime.user.User
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException

class AuthenticationService {

    def springSecurityService
    AuthenticationManager authenticationManager

    User getCurrentUser() {
        springSecurityService.currentUser as User
    }

    @Transactional(readOnly = true)
    boolean authenticateUser(String username, String password) {
        return authenticate(username, password)
    }

    @Transactional(readOnly = true)
    boolean authenticateClient(String clientId, String clientSecret) {
        return authenticate(clientId, clientSecret)
    }

    @Transactional(readOnly = true)
    private boolean authenticate(String principal, String credentials) {
        try {
            def authentication = new UsernamePasswordAuthenticationToken(principal, credentials)
            authenticationManager.authenticate(authentication)
            return true
        }
        catch(AuthenticationException e) {
            log.warn("Failed to authenticate principal [$principal]: ${e.message}")
            return false
        }
    }
}
