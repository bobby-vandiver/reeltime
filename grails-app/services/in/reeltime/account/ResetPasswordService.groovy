package in.reeltime.account

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ResetPasswordException
import in.reeltime.user.User

class ResetPasswordService {

    def userService
    def accountCodeGenerationService

    def localizedMessageService
    def mailService

    def fromAddress
    def resetPasswordCodeValidityLengthInMins

    void sendResetPasswordEmail(User user, Locale locale) {
        if(!user.verified) {
            throw new AuthorizationException("Cannot reset a password if the account has not been verified")
        }
        def code = accountCodeGenerationService.generateResetPasswordCode(user)

        def localizedSubject = localizedMessageService.getMessage('account.password.reset.email.subject', locale)
        def localizedMessage = localizedMessageService.getMessage('account.password.reset.email.message', locale, [user.username, code])

        mailService.sendMail(user.email, fromAddress, localizedSubject, localizedMessage)
    }

    void resetPassword(String username, String newPassword, String code) {

        def user = userService.loadUser(username)
        def resetPasswordCode = AccountCode.findByUserAndType(user, AccountCodeType.ResetPassword)

        if(!resetPasswordCode) {
            // TODO: Need to log something about the attempt to reset the password!
            throw new ResetPasswordException("The user has not requested a password reset")
        }
        if(!resetPasswordCode.isCodeCorrect(code)) {
            throw new ResetPasswordException("The reset password code is not correct")
        }
        try {
            if (resetPasswordCode.hasExpiredInMinutes(resetPasswordCodeValidityLengthInMins as int)) {
                throw new ResetPasswordException("The reset password code has expired")
            }
            user.password = newPassword
            userService.storeUser(user)
        }
        finally {
            resetPasswordCode.delete()
        }
    }
}
