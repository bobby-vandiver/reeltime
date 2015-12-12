package in.reeltime.account

import grails.transaction.Transactional

@Transactional
class DevelopmentOnlyAccountService {

    def userService
    def accountManagementService

    void confirmAccountForUser(String username) {
        def user = userService.loadUser(username)
        accountManagementService.verifyUser(user)
        AccountCode.findAllByUserAndType(user, AccountCodeType.AccountConfirmation)*.delete()
    }

    void resetPasswordForUser(String username, String password) {
        def user = userService.loadUser(username)
        accountManagementService.changePassword(user, password)
        AccountCode.findAllByUserAndType(user, AccountCodeType.ResetPassword)*.delete()
    }
}
