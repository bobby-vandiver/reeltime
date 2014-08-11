package in.reeltime.reel

import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.exceptions.UserNotFoundException
import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class ReelController extends AbstractController {

    def reelService

    def listReels(String username) {
        handleSingleParamRequest(username, 'reel.username.required') {
            def reels = reelService.listReels(username)
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshallReelList(reels)
            }
        }
    }

    def addReel(String name) {
        handleSingleParamRequest(name, 'reel.name.required') {
            reelService.addReel(name)
            render(status: SC_CREATED)
        }
    }

    def deleteReel(Long reelId) {
        handleSingleParamRequest(reelId, 'reel.id.required') {
            reelService.deleteReel(reelId)
            render(status: SC_OK)
        }
    }

    private void handleSingleParamRequest(Object paramToCheck, String errorMessageCode, Closure action) {
        paramToCheck ? action() : errorMessageResponse(errorMessageCode, SC_BAD_REQUEST)
    }

    def handleAuthorizationException(AuthorizationException e) {
        exceptionErrorMessageResponse(e, 'reel.unauthorized', SC_FORBIDDEN)
    }

    def handleUserNotFoundException(UserNotFoundException e) {
        exceptionErrorMessageResponse(e, 'reel.unknown.username', SC_BAD_REQUEST)
    }

    def handleReelNotFoundException(ReelNotFoundException e) {
        exceptionErrorMessageResponse(e, 'reel.unknown', SC_BAD_REQUEST)
    }

    def handleInvalidReelNameException(InvalidReelNameException e) {
        exceptionErrorMessageResponse(e, 'reel.invalid.name', SC_BAD_REQUEST)
    }

    // TODO: Use marshallers plugin if this becomes more involved
    private static List marshallReelList(Collection<Reel> reels) {
        reels.collect([]) { reel ->
            [reelId: reel.id, name: reel.name]
        }
    }
}
