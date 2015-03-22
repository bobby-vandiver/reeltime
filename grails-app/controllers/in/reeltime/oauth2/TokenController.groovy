package in.reeltime.oauth2

import in.reeltime.common.AbstractController

class TokenController extends AbstractController {

    def tokenRemovalService

    def revokeAccessToken(AccessTokenCommand command) {
        handleCommandRequest(command) {
            tokenRemovalService.removeAccessToken(command.access_token)
        }
    }
}
