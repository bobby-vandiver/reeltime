package in.reeltime.account

import grails.transaction.Transactional

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

@Transactional
class AccountRegistrationService {

    def userService
    def clientService

    def reelCreationService
    def accountConfirmationService

    RegistrationResult registerUserAndClient(AccountRegistrationCommand command, Locale locale) {

        def username = command.username
        def password = command.password
        def email = command.email
        def displayName = command.display_name
        def clientName = command.client_name

        def clientId = clientService.generateClientId()
        def clientSecret = clientService.generateClientSecret()

        def client = clientService.createAndSaveClient(clientName, clientId, clientSecret)
        def reel = reelCreationService.createAndSaveReel(UNCATEGORIZED_REEL_NAME)

        def user = userService.createAndSaveUser(username, password, displayName, email, client, reel)

        accountConfirmationService.sendConfirmationEmail(user, locale)
        new RegistrationResult(clientId: clientId, clientSecret: clientSecret)
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
