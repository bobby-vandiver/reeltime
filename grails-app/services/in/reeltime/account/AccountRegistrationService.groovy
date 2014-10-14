package in.reeltime.account

import in.reeltime.user.User

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class AccountRegistrationService {

    def userService
    def clientService

    def reelService

    def accountCodeService
    def securityService

    def localizedMessageService
    def mailService

    def fromAddress

    protected static final SALT_LENGTH = 8
    protected static final CONFIRMATION_CODE_LENGTH = 8
    protected static final ALLOWED_CHARACTERS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

    RegistrationResult registerUserAndClient(AccountRegistrationCommand command, Locale locale) {

        def username = command.username
        def password = command.password
        def email = command.email
        def displayName = command.display_name
        def clientName = command.client_name

        def clientId = clientService.generateClientId()
        def clientSecret = clientService.generateClientSecret()

        def client = clientService.createAndSaveClient(clientName, clientId, clientSecret)
        def reel = reelService.createReel(UNCATEGORIZED_REEL_NAME)

        def user = userService.createAndSaveUser(username, password, displayName, email, client, reel)

        sendConfirmationEmail(user, locale)
        new RegistrationResult(clientId: clientId, clientSecret: clientSecret)
    }

    private void sendConfirmationEmail(User user, Locale locale) {

        def code = securityService.generateSecret(CONFIRMATION_CODE_LENGTH, ALLOWED_CHARACTERS)
        def salt = securityService.generateSalt(SALT_LENGTH)

        def hashedCode = accountCodeService.hashAccountCode(code, salt)
        new AccountCode(user: user, code: hashedCode, salt: salt, type: AccountCodeType.AccountConfirmation).save()

        def localizedSubject = localizedMessageService.getMessage('registration.email.subject', locale)
        def localizedMessage = localizedMessageService.getMessage('registration.email.message', locale, [user.username, code])

        mailService.sendMail(user.email, fromAddress, localizedSubject, localizedMessage)
    }

    RegistrationResult registerClientForExistingUser(String username, String clientName) {
        def user = userService.loadUser(username)

        def clientId = clientService.generateClientId()
        def clientSecret = clientService.generateClientSecret()

        def client = clientService.createAndSaveClient(clientName, clientId, clientSecret)
        user.addToClients(client)
        userService.storeUser(user)

        new RegistrationResult(clientId: clientId, clientSecret: clientSecret)
    }
}
