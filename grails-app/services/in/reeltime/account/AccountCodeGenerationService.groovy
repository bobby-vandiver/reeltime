package in.reeltime.account

import in.reeltime.exceptions.AccountCodeException
import in.reeltime.user.User
import static in.reeltime.account.AccountCode.SALT_LENGTH
import static in.reeltime.account.AccountCode.CODE_LENGTH
import static in.reeltime.account.AccountCode.ALLOWED_CHARACTERS

class AccountCodeGenerationService {

    def securityService

    private static final int MAX_ATTEMPTS = 5

    String generateAccountConfirmationCode(User user) {
        generateCode(user, AccountCodeType.AccountConfirmation)
    }

    String generateResetPasswordCode(User user) {
        generateCode(user, AccountCodeType.ResetPassword)
    }

    private String generateCode(User user, AccountCodeType type) {
        def code = securityService.generateSecret(CODE_LENGTH, ALLOWED_CHARACTERS)
        def salt = generateUniqueSalt()

        new AccountCode(user: user, code: code, salt: salt, type: type).save()
        return code
    }

    private byte[] generateUniqueSalt() {
        byte[] salt = null
        boolean generatedUniqueSalt = false

        for(int attempts = 0; attempts < MAX_ATTEMPTS && !generatedUniqueSalt; attempts++) {
            salt = securityService.generateSalt(SALT_LENGTH)
            generatedUniqueSalt = AccountCode.saltIsUnique(salt)
        }

        if(!generatedUniqueSalt) {
            throw new AccountCodeException("Failed to generate a unique salt. Exceeded max attempts")
        }

        return salt
    }
}
