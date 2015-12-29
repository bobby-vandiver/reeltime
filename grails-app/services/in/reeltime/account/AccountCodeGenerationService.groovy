package in.reeltime.account

import in.reeltime.exceptions.AccountCodeException
import in.reeltime.user.User

import static in.reeltime.account.AccountCode.ALLOWED_CHARACTERS
import static in.reeltime.account.AccountCode.CODE_LENGTH

class AccountCodeGenerationService {

    def securityService
    def cryptoService

    def costFactor

    private static final int MAX_ATTEMPTS = 5

    String generateAccountConfirmationCode(User user) {
        generateCode(user, AccountCodeType.AccountConfirmation)
    }

    String generateResetPasswordCode(User user) {
        generateCode(user, AccountCodeType.ResetPassword)
    }

    private String generateCode(User user, AccountCodeType type) {
        def code = securityService.generateSecret(CODE_LENGTH, ALLOWED_CHARACTERS)

        def cost = costFactor as int
        def salt = generateUniqueSalt(cost)

        new AccountCode(user: user, code: code, salt: salt, cost: cost, type: type).save()
        return code
    }

    private String generateUniqueSalt(int cost) {
        String salt = null
        boolean generatedUniqueSalt = false

        for(int attempts = 0; attempts < MAX_ATTEMPTS && !generatedUniqueSalt; attempts++) {
            salt = cryptoService.generateBCryptSalt(cost)
            generatedUniqueSalt = AccountCode.saltIsUnique(salt)
        }

        if(!generatedUniqueSalt) {
            throw new AccountCodeException("Failed to generate a unique salt. Exceeded max attempts")
        }

        return salt
    }
}
