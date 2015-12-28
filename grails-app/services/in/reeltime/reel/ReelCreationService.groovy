package in.reeltime.reel

import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.user.User

class ReelCreationService {

    def reelService
    def userService

    def activityService
    def authenticationService

    Reel createReel(String reelName) {
        new Reel(name: reelName)
    }

    Reel createReelForUser(User owner, String reelName) {
        def reel = createReel(reelName)
        reel.owner = owner
        return reel
    }

    Reel addReel(String reelName) {
        def currentUser = authenticationService.currentUser
        if(currentUser.hasReel(reelName)) {
            throw new InvalidReelNameException("Reel named [$reelName] already exists")
        }

        def reel = createReelForUser(currentUser, reelName)
        currentUser.addToReels(reel)

        userService.storeUser(currentUser)
        reelService.storeReel(reel)

        activityService.reelCreated(currentUser, reel)
        return reel
    }
}
