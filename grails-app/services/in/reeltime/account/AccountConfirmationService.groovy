package in.reeltime.account

import in.reeltime.exceptions.ConfirmationException
import in.reeltime.user.User

import java.security.MessageDigest

class AccountConfirmationService {

    def userService
    def userAuthenticationService

    def confirmationCodeValidityLengthInDays

    void confirmAccount(String code) {
        def currentUser = userAuthenticationService.currentUser

        def accountConfirmation = findAccountConfirmationForUser(currentUser)
        checkExpiration(accountConfirmation, currentUser)

        if(!accountConfirmation.isCodeCorrect(code)) {
            throw new ConfirmationException("The confirmation code is not correct")
        }
        verifyUser(currentUser)
        accountConfirmation.delete()
    }

    private static AccountCode findAccountConfirmationForUser(User user) {
        def accountConfirmation = AccountCode.findByUserAndType(user, AccountCodeType.AccountConfirmation)
        if(!accountConfirmation) {
            throw new ConfirmationException("The confirmation code is not associated with user [${user.username}]")
        }
        return accountConfirmation
    }

    private void checkExpiration(AccountCode accountConfirmation, User user) {
        if(accountConfirmation.checkExpirationInDays(confirmationCodeValidityLengthInDays as int)) {
            accountConfirmation.delete()
            throw new ConfirmationException("The confirmation code for user [${user.username}] has expired")
        }
    }

    private void verifyUser(User user) {
        user.verified = true
        userService.storeUser(user)
    }
}
