package in.reeltime.account

import in.reeltime.user.User

class AccountRemovalService {

    def userService
    def tokenRemovalService

    void removeAccountForCurrentUser() {
        def currentUser = userService.currentUser
        def username = currentUser.username

        log.info "Removing confirmation codes for user [${username}]"
        deleteConfirmationCodesForUser(currentUser)

        log.info "Removing tokens associated with user [${username}]"
        tokenRemovalService.removeAllTokensForUser(currentUser)

        log.info "Removing clients for user [${username}]"
        deleteClientsForUser(currentUser)

        log.info "Deleting user [${username}]"
        currentUser.delete()

        log.info "Finished removing account for user [${username}]"
    }

    private static void deleteConfirmationCodesForUser(User user) {
        def confirmationCodes = AccountConfirmation.findAllByUser(user)
        confirmationCodes.each { code ->
            log.debug "Deleting account confirmation code [${code.id}]"
            code.delete()
        }
    }

    private static void deleteClientsForUser(User user) {
        user.clients.each { client ->
            log.debug "Deleting client [${client.id}]"
            client.delete()
        }
    }
}
