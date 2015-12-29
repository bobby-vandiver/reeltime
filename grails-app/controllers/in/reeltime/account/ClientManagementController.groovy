package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.RegistrationException
import in.reeltime.search.PagedListCommand
import in.reeltime.user.User

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class ClientManagementController extends AbstractController {

    def accountRegistrationService
    def accountManagementService
    def authenticationService
    def clientService

    @Secured(["permitAll"])
    def registerClient(ClientRegistrationCommand command) {
        handleCommandRequest(command) {
            try {
                render(status: SC_CREATED, contentType: APPLICATION_JSON) {
                    marshall(accountRegistrationService.registerClientForExistingUser(command.username, command.client_name))
                }
            }
            catch(RegistrationException e) {
                exceptionErrorMessageResponse(e, 'registration.internal.error', SC_SERVICE_UNAVAILABLE)
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-read')"])
    def listClients(PagedListCommand command) {
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(clients: clientService.listClientsForUser(currentUser, command.page))
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def revokeClient(RevokeClientCommand command) {
        log.debug "Revoking access for client [${command.client_id}]"
        handleCommandRequest(command) {
            accountManagementService.revokeClient(currentUser, command.client_id)
            render(status: SC_OK)
        }
    }

    private User getCurrentUser() {
        authenticationService.currentUser
    }
}
