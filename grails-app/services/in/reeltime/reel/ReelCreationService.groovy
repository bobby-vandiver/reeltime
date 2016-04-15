package in.reeltime.reel

import grails.transaction.Transactional
import in.reeltime.exceptions.InvalidReelNameException

@Transactional
class ReelCreationService {

    def reelService
    def userService

    def activityService
    def authenticationService

    Reel createAndSaveReel(String reelName) {
        def reel = new Reel(name: reelName)
        reelService.storeReel(reel)
        return reel
    }

    Reel addReel(String reelName) {
        def currentUser = authenticationService.currentUser
        if(currentUser.hasReel(reelName)) {
            throw new InvalidReelNameException("Reel named [$reelName] already exists")
        }

        def reel = createAndSaveReel(reelName)
        new UserReel(owner: currentUser, reel: reel).save()

        activityService.reelCreated(currentUser, reel)
        return reel
    }
}
