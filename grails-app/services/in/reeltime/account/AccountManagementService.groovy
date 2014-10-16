package in.reeltime.account

import in.reeltime.user.User

class AccountManagementService {

    def userService

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
        user.clients.remove(client)
        client.delete()

        userService.storeUser(user)
    }
}
