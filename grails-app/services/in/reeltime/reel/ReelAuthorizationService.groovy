package in.reeltime.reel

import static in.reeltime.reel.Reel.*

class ReelAuthorizationService {

    def authenticationService

    boolean reelNameIsReserved(String reelName) {
        return reelName?.toLowerCase() == UNCATEGORIZED_REEL_NAME.toLowerCase()
    }

    boolean reelNameIsValidLength(String reelName) {
        def length = reelName?.length()
        return length >= MINIMUM_NAME_LENGTH && length <= MAXIMUM_NAME_LENGTH
    }

    boolean currentUserIsReelOwner(Reel reel) {
        return reel.owner == authenticationService.currentUser
    }
}
