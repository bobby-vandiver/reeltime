package in.reeltime.account

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ConfirmationException
import in.reeltime.user.User

class AccountConfirmationService {

    def accountManagementService
    def accountCodeGenerationService

    def authenticationService

    def localizedMessageService
    def emailManager

    def fromAddress
    def confirmationCodeValidityLengthInDays

    void sendConfirmationEmail(User user, Locale locale) {
        def code = accountCodeGenerationService.generateAccountConfirmationCode(user)

        def localizedSubject = localizedMessageService.getMessage('registration.email.subject', locale)
        def localizedMessage = localizedMessageService.getMessage('registration.email.message', locale, [user.username, code])

        emailManager.sendMail(user.email, fromAddress, localizedSubject, localizedMessage)
    }

    void confirmAccount(String code) {

        def currentUser = authenticationService.currentUser
        def accountConfirmationCodes = AccountCode.findAllByUserAndType(currentUser, AccountCodeType.AccountConfirmation)

        def username = currentUser.username

        if(!accountConfirmationCodes) {
            throw new AuthorizationException("The confirmation code is not associated with user [${username}]")
        }

        def accountConfirmation = accountConfirmationCodes.find { it.isCodeCorrect(code) }
        if(!accountConfirmation) {
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
