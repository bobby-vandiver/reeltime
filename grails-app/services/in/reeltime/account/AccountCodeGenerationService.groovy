package in.reeltime.account

import in.reeltime.user.User

class AccountCodeGenerationService {

    def securityService

    protected static final SALT_LENGTH = 8
    protected static final CODE_LENGTH = 8
    protected static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

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
