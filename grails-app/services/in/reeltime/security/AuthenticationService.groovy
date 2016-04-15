package in.reeltime.security

import grails.plugin.springsecurity.SpringSecurityService
import grails.transaction.Transactional
import in.reeltime.oauth2.Client
import in.reeltime.user.User
import org.springframework.security.authentication.encoding.PasswordEncoder

@Transactional
class AuthenticationService {

    SpringSecurityService springSecurityService
    PasswordEncoder passwordEncoder

    User getCurrentUser() {
        springSecurityService.currentUser as User
    }

    boolean authenticateUser(String username, String password) {
        def user = User.findByUsername(username)
        return user && checkCredentials(user.password, password)
    }

    boolean authenticateClient(String clientId, String clientSecret) {
        def client = Client.findByClientId(clientId)
        return client && checkCredentials(client.clientSecret, clientSecret)
    }

    private boolean checkCredentials(String encryptedCredentials, String plainTextCredentials) {
        return passwordEncoder.isPasswordValid(encryptedCredentials, plainTextCredentials, null)
    }
}
