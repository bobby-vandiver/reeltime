package in.reeltime.reel

import in.reeltime.common.AbstractController
import in.reeltime.exceptions.InvalidReelNameException

import static javax.servlet.http.HttpServletResponse.*

class ReelController extends AbstractController {

    def reelService

    def addReel(String name) {
        reelService.addReel(name)
        render(status: SC_CREATED)
    }

    def handleInvalidReelNameException(InvalidReelNameException e) {
        handleErrorMessageResponse(e, 'reel.invalid.name', SC_BAD_REQUEST)
    }
}
