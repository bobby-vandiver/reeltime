package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.user.User

class ReelService {

    def userService
    def reelAuthorizationService

    Reel createReel(String reelName) {
        def audience = new Audience(users: [])
        new Reel(name: reelName, audience: audience, videos: [])
    }

    Reel createReelForUser(User owner, String reelName) {
        def reel = createReel(reelName)
        reel.owner = owner
        return reel
    }

    Reel loadReel(Long reelId) {
        def reel = Reel.findById(reelId)
        if(!reel) {
            throw new ReelNotFoundException("Reel [$reelId] not found")
        }
        return reel
    }

    void storeReel(Reel reel) {
        reel.save()
    }

    Collection<Reel> listReels(String username) {
        userService.loadUser(username).reels
    }

    void addReel(String reelName) {
        if(reelAuthorizationService.reelNameIsReserved(reelName)) {
            throw new IllegalArgumentException("Reel name [$reelName] is reserved")
        }
        def currentUser = userService.currentUser
        def reel = createReelForUser(currentUser, reelName)

        currentUser.addToReels(reel)
        userService.storeUser(currentUser)
    }

    void deleteReel(Long reelId) {
        def reel = loadReel(reelId)
        def name = reel.name

        if(!reelAuthorizationService.currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Only the owner of a reel can delete it")
        }
        else if(reelAuthorizationService.reelNameIsReserved(name)) {
            throw new AuthorizationException("The ${name} reel cannot be deleted")
        }
        reel.owner.removeFromReels(reel)
        reel.delete()
    }
}
