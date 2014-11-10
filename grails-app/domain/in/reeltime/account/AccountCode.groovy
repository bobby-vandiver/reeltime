package in.reeltime.account

import in.reeltime.user.User

import java.security.MessageDigest

class AccountCode {

    static final SALT_LENGTH = 32
    static final CODE_LENGTH = 8
    static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

    User user
    String code
    byte[] salt

    AccountCodeType type
    Date dateCreated

    static boolean saltIsUnique(byte[] salt) {
        AccountCode.countBySalt(salt) == 0
    }

    static constraints = {
        user nullable: false
        code blank: false, nullable: false
        salt nullable: false, minSize: SALT_LENGTH, maxSize: SALT_LENGTH
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

    boolean hasExpiredInDays(int validityLengthInDays) {
        hasExpired(Calendar.DAY_OF_MONTH, validityLengthInDays)
    }

    boolean hasExpiredInMinutes(int validityLengthInMinutes) {
        hasExpired(Calendar.MINUTE, validityLengthInMinutes)
    }

    private hasExpired(int field, int amount) {
        Calendar calendar = Calendar.instance
        calendar.add(field, -1 * amount)
        return dateCreated.time < calendar.timeInMillis
    }

    private static String hashAndSaltCode(String code, byte[] salt) {
        MessageDigest messageDigest = MessageDigest.getInstance('SHA-256')
        messageDigest.update(code.getBytes('utf-8'))
        messageDigest.update(salt)
        messageDigest.digest().toString()
    }
}
