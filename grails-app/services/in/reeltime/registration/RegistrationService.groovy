package in.reeltime.registration

import in.reeltime.exceptions.VerificationException
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
    def verificationCodeValidityLengthInDays

    protected static final SALT_LENGTH = 8
    protected static final VERIFICATION_CODE_LENGTH = 8
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

    void sendVerificationEmail(String username, String email, Locale locale) {

        def code = securityService.generateSecret(VERIFICATION_CODE_LENGTH, ALLOWED_CHARACTERS)
        def salt = securityService.generateSalt(SALT_LENGTH)

        def hashedCode = hashVerificationCode(code, salt)
        def user = User.findByUsernameAndEmail(username, email)

        new AccountVerification(user: user, code: hashedCode, salt: salt).save()

        def localizedSubject = localizedMessageService.getMessage('registration.email.subject', locale)
        def localizedMessage = localizedMessageService.getMessage('registration.email.message', locale, [username, code])

        mailService.sendMail {
            to email
            from fromAddress
            subject localizedSubject
            body localizedMessage
        }
    }

    void verifyAccount(String code) {
        def currentUser = springSecurityService.currentUser as User

        def accountVerification = findAccountVerificationForUser(currentUser)
        checkExpiration(accountVerification, currentUser)

        def hash = accountVerification.code
        def salt = accountVerification.salt

        if(verificationCodeIsCorrect(code, hash, salt)) {
            verifyUser(currentUser)
            accountVerification.delete()
        }
    }

    private static AccountVerification findAccountVerificationForUser(User user) {
        def accountVerification = AccountVerification.findByUser(user)
        if(!accountVerification) {
            throw new VerificationException("The verification code is not associated with user [${user.username}]")
        }
        return accountVerification
    }

    private void checkExpiration(AccountVerification accountVerification, User user) {
        def dateCreated = accountVerification.dateCreated
        if(verificationCodeHasExpired(dateCreated)) {
            accountVerification.delete()
            throw new VerificationException("The verification code for user [${user.username}] has expired")
        }
    }

    private boolean verificationCodeHasExpired(Date dateCreated) {
        Calendar calendar = Calendar.instance
        calendar.add(Calendar.DAY_OF_MONTH, -1 * verificationCodeValidityLengthInDays as int)
        return dateCreated.time < calendar.timeInMillis
    }

    private void verifyUser(User user) {
        user.verified = true
        userService.updateUser(user)
    }

    private static boolean verificationCodeIsCorrect(String rawCode, String storedCode, byte[] salt) {
        hashVerificationCode(rawCode, salt) == storedCode
    }

    protected static String hashVerificationCode(String code, byte[] salt) {
        MessageDigest messageDigest = MessageDigest.getInstance('SHA-256')
        messageDigest.update(code.getBytes('utf-8'))
        messageDigest.update(salt)
        messageDigest.digest().toString()
    }
}
