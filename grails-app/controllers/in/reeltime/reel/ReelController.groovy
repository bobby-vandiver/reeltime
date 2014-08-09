package in.reeltime.reel

import in.reeltime.exceptions.InvalidReelNameException

import static javax.servlet.http.HttpServletResponse.*

class ReelController {

    def reelService
    def localizedMessageService

    def addReel(String name) {
        reelService.addReel(name)
        render(status: SC_CREATED)
    }

    def handleInvalidReelNameException(InvalidReelNameException e) {
        log.warn("Handling InvalidReelNameException: ", e)
        def message = localizedMessageService.getMessage('reel.invalid.name', request.locale)

        render(status: SC_BAD_REQUEST, contentType: 'application/json') {
            [errors: [message]]
        }
    }
}
