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
}
