package in.reeltime.registration

import in.reeltime.exceptions.ConfirmationException
import in.reeltime.user.User
import java.security.MessageDigest

class RegistrationService {

    def userService
    def clientService

    def securityService
    def springSecurityService

    def localizedMessageService
    def mailService

    def fromAddress
    def confirmationCodeValidityLengthInDays

    protected static final SALT_LENGTH = 8
    protected static final CONFIRMATION_CODE_LENGTH = 8
    protected static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

    RegistrationResult registerUserAndClient(RegistrationCommand command) {

        def username = command.username
        def password = command.password
        def email = command.email
        def clientName = command.client_name

        def clientId = clientService.generateClientId()
        def clientSecret = clientService.generateClientSecret()

        def client = clientService.createClient(clientName, clientId, clientSecret)
        userService.createUser(username, password, email, client)

        new RegistrationResult(clientId: clientId, clientSecret: clientSecret)
    }

    void sendConfirmationEmail(String username, String email, Locale locale) {

        def code = securityService.generateSecret(CONFIRMATION_CODE_LENGTH, ALLOWED_CHARACTERS)
        def salt = securityService.generateSalt(SALT_LENGTH)

        def hashedCode = hashConfirmationCode(code, salt)
        def user = User.findByUsernameAndEmail(username, email)

        new AccountConfirmation(user: user, code: hashedCode, salt: salt).save()

        def localizedSubject = localizedMessageService.getMessage('registration.email.subject', locale)
        def localizedMessage = localizedMessageService.getMessage('registration.email.message', locale, [username, code])

        mailService.sendMail(email, fromAddress, localizedSubject, localizedMessage)
    }

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
        userService.updateUser(user)
    }

    private static boolean confirmationCodeIsCorrect(String rawCode, String storedCode, byte[] salt) {
        hashConfirmationCode(rawCode, salt) == storedCode
    }

    protected static String hashConfirmationCode(String code, byte[] salt) {
        MessageDigest messageDigest = MessageDigest.getInstance('SHA-256')
        messageDigest.update(code.getBytes('utf-8'))
        messageDigest.update(salt)
        messageDigest.digest().toString()
    }
}
