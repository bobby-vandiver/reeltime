package in.reeltime.reel

import grails.transaction.Transactional

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

@Transactional
class ReelAuthorizationService {

    def authenticationService

    boolean reelNameIsReserved(String reelName) {
        return reelName?.toLowerCase() == UNCATEGORIZED_REEL_NAME.toLowerCase()
    }

    boolean currentUserIsReelOwner(Reel reel) {
        return reel.owner == authenticationService.currentUser
    }
}
