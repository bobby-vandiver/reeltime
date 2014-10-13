package in.reeltime.account

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User

class AccountManagementService {

    def userService

    def localizedMessageService
    def mailService

    def fromAddress

    void changePassword(User user, String password) {
        user.password = password
        userService.storeUser(user)
    }

    void changeDisplayName(User user, String displayName) {
        user.displayName = displayName
        userService.storeUser(user)
    }

    // TODO: Create ResetPassword domain object and save an instance and include the reset code in the email
    void sendResetPasswordEmail(User user, Locale locale) {
        if(!user.verified) {
            throw new AuthorizationException("Cannot reset a password if the account has not been verified")
        }
        def localizedSubject = localizedMessageService.getMessage('account.password.reset.email.subject', locale)
        def localizedMessage = ''

        mailService.sendMail(user.email, fromAddress, localizedSubject, localizedMessage)
    }
}
