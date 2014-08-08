package in.reeltime.reel

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class ReelAuthorizationService {

    def userService

    boolean reelNameIsReserved(String reelName) {
        return reelName.toLowerCase() == UNCATEGORIZED_REEL_NAME.toLowerCase()
    }

    boolean currentUserIsNotReelOwner(Reel reel) {
        return reel.owner != userService.currentUser
    }
}
