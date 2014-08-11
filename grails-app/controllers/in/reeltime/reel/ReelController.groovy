package in.reeltime.reel

import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.exceptions.UserNotFoundException

import static javax.servlet.http.HttpServletResponse.*

class ReelController extends AbstractController {

    def reelService

    def listReels(String username) {

        if(username) {
            def reels = reelService.listReels(username)
            render(status: SC_OK, contentType: JSON_CONTENT_TYPE) {
                marshallReelList(reels)
            }
        }
        else {
            errorMessageResponse('reel.username.required', SC_BAD_REQUEST)
        }
    }

    def addReel(String name) {

        if(name) {
            reelService.addReel(name)
            render(status: SC_CREATED)
        }
        else {
            errorMessageResponse('reel.name.required', SC_BAD_REQUEST)
        }
    }

    def deleteReel(Long reelId) {

        if(reelId) {
            reelService.deleteReel(reelId)
            render(status: SC_OK)
        }
        else {
            errorMessageResponse('reel.id.required', SC_BAD_REQUEST)
        }
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
