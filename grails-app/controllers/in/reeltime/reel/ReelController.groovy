package in.reeltime.reel

import in.reeltime.common.AbstractController
import in.reeltime.exceptions.InvalidReelNameException

import static javax.servlet.http.HttpServletResponse.*

class ReelController extends AbstractController {

    def reelService

    def addReel(String name) {

        if(name) {
            reelService.addReel(name)
            render(status: SC_CREATED)
        }
        else {
            errorMessageResponse('reel.name.required', SC_BAD_REQUEST)
        }
    }

    def handleInvalidReelNameException(InvalidReelNameException e) {
        exceptionErrorMessageResponse(e, 'reel.invalid.name', SC_BAD_REQUEST)
    }
}
