package in.reeltime.account

import in.reeltime.exceptions.ConfirmationException
import in.reeltime.user.User

import java.security.MessageDigest

class AccountConfirmationService {

    def userService
    def userAuthenticationService

    def accountCodeService
    def confirmationCodeValidityLengthInDays

    void confirmAccount(String code) {
        def currentUser = userAuthenticationService.currentUser

        def accountConfirmation = findAccountConfirmationForUser(currentUser)
        checkExpiration(accountConfirmation, currentUser)

        def hash = accountConfirmation.code
        def salt = accountConfirmation.salt

        if(!accountCodeService.accountCodeIsCorrect(code, hash, salt)) {
            throw new ConfirmationException("The confirmation code [$code] is not correct")
        }
        verifyUser(currentUser)
        accountConfirmation.delete()
    }

    private static AccountCode findAccountConfirmationForUser(User user) {
        def accountConfirmation = AccountCode.findByUser(user)
        if(!accountConfirmation) {
            throw new ConfirmationException("The confirmation code is not associated with user [${user.username}]")
        }
        return accountConfirmation
    }

    private void checkExpiration(AccountCode accountConfirmation, User user) {
        def dateCreated = accountConfirmation.dateCreated
        if(confirmationCodeHasExpired(dateCreated)) {
            accountConfirmation.delete()
            throw new ConfirmationException("The confirmation code for user [${user.username}] has expired")
        }
    }

    private boolean confirmationCodeHasExpired(Date dateCreated) {
        Calendar calendar = Calendar.instance
        calendar.add(Calendar.DAY_OF_MONTH, -1 * confirmationCodeValidityLengthInDays as int)
        return dateCreated.time < calendar.timeInMillis
    }

    private void verifyUser(User user) {
        user.verified = true
        userService.storeUser(user)
    }
}
