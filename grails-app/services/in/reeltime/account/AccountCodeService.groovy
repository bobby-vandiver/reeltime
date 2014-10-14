package in.reeltime.account

import java.security.MessageDigest

class AccountCodeService {

    boolean accountCodeIsCorrect(String rawCode, String storedCode, byte[] salt) {
        hashAccountCode(rawCode, salt) == storedCode
    }

    String hashAccountCode(String code, byte[] salt) {
        MessageDigest messageDigest = MessageDigest.getInstance('SHA-256')
        messageDigest.update(code.getBytes('utf-8'))
        messageDigest.update(salt)
        messageDigest.digest().toString()
    }
}
