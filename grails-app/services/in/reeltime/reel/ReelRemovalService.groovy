package in.reeltime.reel

import grails.transaction.Transactional
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User

@Transactional
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

        removeOwnership(reel)
    }

    void removeAllReelsForUser(User user) {
        Collection<Reel> reelsToRemove = []
        reelsToRemove.addAll(user.reels)

        reelsToRemove.each { reel ->
            prepareReelForRemoval(reel)
            removeOwnership(reel)
        }
    }

    private void removeOwnership(Reel reel) {
        def owner = reel.owner
        activityService.reelDeleted(owner, reel)

        UserReel.findByOwnerAndReel(owner, reel).delete()
        reel.delete()
    }
}
