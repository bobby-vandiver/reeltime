package in.reeltime.security

import java.security.SecureRandom

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

    byte[] generateSalt(int size) {
        def secureRandom = new SecureRandom()
        def salt = new byte[size]
        secureRandom.nextBytes(salt)
        return salt
    }
}
