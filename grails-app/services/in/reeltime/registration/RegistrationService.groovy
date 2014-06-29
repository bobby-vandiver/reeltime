package in.reeltime.registration

import in.reeltime.user.User
import org.springframework.security.crypto.keygen.KeyGenerators

import java.security.MessageDigest

class RegistrationService {

    def userService
    def clientService

    def secretService

    def localizedMessageService
    def mailService

    protected static final FROM_ADDRESS = 'registration@reeltime.in'

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

        def code = secretService.generateSecret(VERIFICATION_CODE_LENGTH, ALLOWED_CHARACTERS)
        def salt = KeyGenerators.secureRandom().generateKey()

        def hashedCode = hashVerificationCode(code, salt)
        def user = User.findByUsernameAndEmail(username, email)

        new AccountVerification(user: user, code: hashedCode, salt: salt).save()

        def localizedSubject = localizedMessageService.getMessage('registration.email.subject', locale)
        def localizedMessage = localizedMessageService.getMessage('registration.email.message', locale, [username, code])

        mailService.sendMail {
            to email
            from FROM_ADDRESS
            subject localizedSubject
            body localizedMessage
        }
    }

    protected static String hashVerificationCode(String code, byte[] salt) {
        MessageDigest messageDigest = MessageDigest.getInstance('SHA-256')
        messageDigest.update(code.getBytes('utf-8'))
        messageDigest.update(salt)
        messageDigest.digest().toString()
    }
}
