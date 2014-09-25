package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User


class AudienceService {

    def reelService
    def reelAuthorizationService

    def userService

    Collection<User> listMembers(Long reelId) {
        def reel = reelService.loadReel(reelId)
        return reel.audience.members
    }

    List<Reel> listReelsForAudienceMember(User user) {
        def audiences = Audience.findAllByAudienceMember(user)
        return audiences*.reel
    }

    void addMember(Long reelId) {
        def reel = reelService.loadReel(reelId)
        if(reelAuthorizationService.currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Owner of a reel cannot be a member of the reel's audience")
        }
        def currentUser = userService.currentUser
        reel.audience.addToMembers(currentUser)
        reelService.storeReel(reel)
    }

    void removeMember(Long reelId) {
        def reel = reelService.loadReel(reelId)
        def audience = reel.audience

        def currentUser = userService.currentUser
        if(!audience.hasMember(currentUser)) {
            def message = "Current user [${currentUser.username}] is not a member of the audience for reel [$reelId]"
            throw new AuthorizationException(message)
        }
        audience.removeFromMembers(currentUser)
        reelService.storeReel(reel)
    }
}
