package in.reeltime.security

import grails.transaction.Transactional
import org.springframework.security.crypto.bcrypt.BCrypt

@Transactional
class CryptoService {

    String generateBCryptSalt(int cost) {
        BCrypt.gensalt(cost)
    }

    String hashBCrypt(String password, String salt) {
        BCrypt.hashpw(password, salt)
    }

    boolean checkBCrypt(String plainText, String hashed) {
        BCrypt.checkpw(plainText, hashed)
    }
}
