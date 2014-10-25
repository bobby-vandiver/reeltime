package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.RegistrationException
import in.reeltime.user.User

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.SC_CREATED
import static javax.servlet.http.HttpServletResponse.SC_OK
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE

class ClientManagementController extends AbstractController {

    def accountRegistrationService
    def accountManagementService
    def authenticationService

    static allowedMethods = [registerClient: 'POST', revokeClient: 'DELETE']

    @Secured(["permitAll"])
    def registerClient(ClientRegistrationCommand command) {
        handleCommandRequest(command) {
            render(status: SC_CREATED, contentType: APPLICATION_JSON) {
                marshall(accountRegistrationService.registerClientForExistingUser(command.username, command.client_name))
            }
        }
    }
    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def revokeClient(String client_id) {
        handleSingleParamRequest(client_id, 'account.revoke.client.id.required') {
            accountManagementService.revokeClient(currentUser, client_id)
            render(status: SC_OK)
        }
    }

    private User getCurrentUser() {
        authenticationService.currentUser
    }

    def handleRegistrationException(RegistrationException e) {
        exceptionErrorMessageResponse(e, 'registration.internal.error', SC_SERVICE_UNAVAILABLE)
    }

}
