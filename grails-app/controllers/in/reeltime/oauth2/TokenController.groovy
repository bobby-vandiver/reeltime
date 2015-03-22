package in.reeltime.oauth2

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController

import static javax.servlet.http.HttpServletResponse.*

class TokenController extends AbstractController {

    def tokenRemovalService

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('account-write')"])
    def revokeAccessToken(AccessTokenCommand command) {
        handleCommandRequest(command) {
            tokenRemovalService.removeAccessToken(command.access_token)
            render(status: SC_OK)
        }
    }
}
