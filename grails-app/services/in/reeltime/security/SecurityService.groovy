package in.reeltime.security

import grails.transaction.Transactional

import java.security.SecureRandom

@Transactional
class SecurityService {

    String generateSecret(int requiredLength, String allowedCharacters) {
        def secureRandom = new SecureRandom()
        def secret = new StringBuilder()

        requiredLength.times {
            def idx = secureRandom.nextInt(allowedCharacters.size())
            secret.append(allowedCharacters[idx])
        }
        return secret.toString()
    }
}
