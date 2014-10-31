package in.reeltime.account

import in.reeltime.user.User
import static in.reeltime.account.AccountCode.*

class AccountCodeGenerationService {

    def securityService

    String generateAccountConfirmationCode(User user) {
        generateCode(user, AccountCodeType.AccountConfirmation)
    }

    String generateResetPasswordCode(User user) {
        generateCode(user, AccountCodeType.ResetPassword)
    }

    private String generateCode(User user, AccountCodeType type) {
        def code = securityService.generateSecret(CODE_LENGTH, ALLOWED_CHARACTERS)
        def salt = securityService.generateSalt(SALT_LENGTH)

        new AccountCode(user: user, code: code, salt: salt, type: type).save()
        return code
    }
}
