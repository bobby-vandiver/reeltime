package in.reeltime.registration

import in.reeltime.user.User

class RegistrationService {

    def userService
    def clientService

    def secretService

    def localizedMessageService
    def mailService

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

        def verificationCode = secretService.generateSecret(VERIFICATION_CODE_LENGTH, ALLOWED_CHARACTERS)

        def user = User.findByUsernameAndEmail(username, email)
        new AccountVerification(user: user, code: verificationCode).save()

        def localizedSubject = localizedMessageService.getMessage('registration.email.subject', locale)
        def localizedMessage = localizedMessageService.getMessage('registration.email.message', locale, [username, verificationCode])

        mailService.sendMail {
            to email
            from 'registration@reeltime.in'
            subject localizedSubject
            body localizedMessage
        }
    }
}
