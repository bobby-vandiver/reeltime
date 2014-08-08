package in.reeltime.account

import in.reeltime.exceptions.ConfirmationException
import in.reeltime.user.User

import java.security.MessageDigest

class AccountConfirmationService {

    def userService
    def springSecurityService
    def confirmationCodeValidityLengthInDays

    void confirmAccount(String code) {
        def currentUser = springSecurityService.currentUser as User

        def accountConfirmation = findAccountConfirmationForUser(currentUser)
        checkExpiration(accountConfirmation, currentUser)

        def hash = accountConfirmation.code
        def salt = accountConfirmation.salt

        if(confirmationCodeIsCorrect(code, hash, salt)) {
            verifyUser(currentUser)
            accountConfirmation.delete()
        }
    }

    private static AccountConfirmation findAccountConfirmationForUser(User user) {
        def accountConfirmation = AccountConfirmation.findByUser(user)
        if(!accountConfirmation) {
            throw new ConfirmationException("The confirmation code is not associated with user [${user.username}]")
        }
        return accountConfirmation
    }

    private void checkExpiration(AccountConfirmation accountConfirmation, User user) {
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

    private boolean confirmationCodeIsCorrect(String rawCode, String storedCode, byte[] salt) {
        hashConfirmationCode(rawCode, salt) == storedCode
    }

    String hashConfirmationCode(String code, byte[] salt) {
        MessageDigest messageDigest = MessageDigest.getInstance('SHA-256')
        messageDigest.update(code.getBytes('utf-8'))
        messageDigest.update(salt)
        messageDigest.digest().toString()
    }
}
