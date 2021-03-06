package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.user.User

import static javax.servlet.http.HttpServletResponse.SC_OK

class AccountManagementController extends AbstractController {

    def accountManagementService
    def authenticationService

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def changePassword(ChangePasswordCommand command) {
        handleCommandRequest(command) {
            accountManagementService.changePassword(currentUser, command.new_password)
            render(status: SC_OK)
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def changeDisplayName(ChangeDisplayNameCommand command) {
        handleCommandRequest(command) {
            accountManagementService.changeDisplayName(currentUser, command.new_display_name)
            render(status: SC_OK)
        }
    }

    private User getCurrentUser() {
        authenticationService.currentUser
    }
}
