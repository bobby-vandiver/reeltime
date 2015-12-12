package in.reeltime.account

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.user.User

@ToString(includeNames = true, excludes = 'code,salt')
@EqualsAndHashCode(includes = ['user', 'code', 'salt'])
class AccountCode {

    static final CODE_LENGTH = 43
    static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

    transient cryptoService

    User user
    String code

    String salt
    Integer cost

    AccountCodeType type
    Date dateCreated

    static boolean saltIsUnique(String salt) {
        AccountCode.countBySalt(salt) == 0
    }

    static constraints = {
        user nullable: false
        code blank: false, nullable: false
        salt blank: false, nullable: false
        cost nullable: false, min: 4, max: 31
        type nullable: false
    }

    static transients = ['cryptoService']

    def beforeInsert() {
        code = hashAndSaltCode(code, salt)
    }

    def beforeUpdate() {
        if(isDirty('code')) {
            code = hashAndSaltCode(code, salt)
        }
    }

    boolean isCodeCorrect(String plainTextCode) {
        cryptoService.checkBCrypt(plainTextCode, code)
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

    // Temporarily making the parameters untyped as a workaround for GROOVY-7368
    // so the unit tests for AccountCodeGenerationService can override this method.
    //
    // GROOVY-7368: https://issues.apache.org/jira/browse/GROOVY-7368

    private String hashAndSaltCode(/*String*/ code, /*String*/ salt) {
        cryptoService.hashBCrypt(code, salt)
    }
}
