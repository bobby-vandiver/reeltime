package in.reeltime.account

import grails.transaction.Transactional
import in.reeltime.exceptions.ClientNotFoundException
import in.reeltime.user.User

@Transactional
class AccountManagementService {

    def userService
    def tokenRemovalService

    void changePassword(User user, String password) {
        user.password = password
        userService.storeUser(user)
    }

    void changeDisplayName(User user, String displayName) {
        user.displayName = displayName
        userService.storeUser(user)
    }

    void verifyUser(User user) {
        user.verified = true
        userService.storeUser(user)
    }

    void revokeClient(User user, String clientId) {
        def client = user.clients.find {
            it.clientId == clientId
        }
        if(!client) {
            throw new ClientNotFoundException("Cannot revoke unknown client")
        }

        tokenRemovalService.removeAllTokensForClient(client)
        user.clients.remove(client)
        client.delete()

        userService.storeUser(user)
    }
}
