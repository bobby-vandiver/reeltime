package in.reeltime.account

import in.reeltime.exceptions.ConfirmationException
import in.reeltime.user.User

class AccountConfirmationService {

    def accountManagementService
    def accountCodeGenerationService

    def userAuthenticationService

    def localizedMessageService
    def mailService

    def fromAddress
    def confirmationCodeValidityLengthInDays

    void sendConfirmationEmail(User user, Locale locale) {
        def code = accountCodeGenerationService.generateAccountConfirmationCode(user)

        def localizedSubject = localizedMessageService.getMessage('registration.email.subject', locale)
        def localizedMessage = localizedMessageService.getMessage('registration.email.message', locale, [user.username, code])

        mailService.sendMail(user.email, fromAddress, localizedSubject, localizedMessage)
    }

    void confirmAccount(String code) {

        def currentUser = userAuthenticationService.currentUser
        def accountConfirmation = AccountCode.findByUserAndType(currentUser, AccountCodeType.AccountConfirmation)

        def username = currentUser.username

        if(!accountConfirmation) {
            throw new ConfirmationException("The confirmation code is not associated with user [${username}]")
        }
        if(!accountConfirmation.isCodeCorrect(code)) {
            throw new ConfirmationException("The confirmation code is not correct")
        }
        try {
            if (accountConfirmation.hasExpiredInDays(confirmationCodeValidityLengthInDays as int)) {
                throw new ConfirmationException("The confirmation code for user [${username}] has expired")
            }
            accountManagementService.verifyUser(currentUser)
        }
        finally {
            accountConfirmation.delete()
        }
    }
}
