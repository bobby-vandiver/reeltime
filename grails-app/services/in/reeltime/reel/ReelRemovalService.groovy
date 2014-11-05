package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User

class ReelRemovalService {

    def audienceService

    def reelAuthorizationService
    def reelVideoManagementService

    def userService
    def activityService

    void prepareReelForRemoval(Reel reel) {
        if(!reelAuthorizationService.currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Only the owner of a reel can delete it")
        }
        reelVideoManagementService.removeAllVideosFromReel(reel)
        audienceService.removeAllMembersFromAudience(reel)
    }

    void removeReel(Reel reel) {
        prepareReelForRemoval(reel)

        def name = reel.name
        if(reelAuthorizationService.reelNameIsReserved(name)) {
            throw new AuthorizationException("The ${name} reel cannot be deleted")
        }

        def owner = reel.owner
        activityService.reelDeleted(owner, reel)

        owner.removeFromReels(reel)
        reel.delete()
        userService.storeUser(owner)
    }

    void removeReelsForUser(User user) {
        Collection<Reel> reelsToRemove = []
        reelsToRemove.addAll(user.reels)

        def uncategorizedReel = reelsToRemove.find { it.name == Reel.UNCATEGORIZED_REEL_NAME }
        reelsToRemove.remove(uncategorizedReel)
        prepareReelForRemoval(uncategorizedReel)

        reelsToRemove.each { reel ->
            removeReel(reel)
        }
    }
}
