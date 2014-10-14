package in.reeltime.account

import in.reeltime.user.User

import java.security.MessageDigest

class AccountCode {

    User user
    String code
    byte[] salt

    AccountCodeType type
    Date dateCreated

    static constraints = {
        user nullable: false
        code blank: false, nullable: false
        salt nullable: false, size: 8..8
        type nullable: false
    }

    def beforeInsert() {
        code = hashAndSaltCode(code, salt)
    }

    def beforeUpdate() {
        if(isDirty('code')) {
            code = hashAndSaltCode(code, salt)
        }
    }

    boolean isCodeCorrect(String plainTextCode) {
        hashAndSaltCode(plainTextCode, salt) == code
    }

    boolean checkExpirationInDays(int validityLengthInDays) {
        Calendar calendar = Calendar.instance
        calendar.add(Calendar.DAY_OF_MONTH, -1 * validityLengthInDays)
        return dateCreated.time < calendar.timeInMillis
    }

    private static String hashAndSaltCode(String code, byte[] salt) {
        MessageDigest messageDigest = MessageDigest.getInstance('SHA-256')
        messageDigest.update(code.getBytes('utf-8'))
        messageDigest.update(salt)
        messageDigest.digest().toString()
    }
}
