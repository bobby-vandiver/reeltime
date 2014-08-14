package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User


class AudienceService {

    def reelService
    def userService

    Collection<User> listMembers(Long reelId) {
        def reel = reelService.loadReel(reelId)
        return reel.audience.members
    }

    void addMember(Long reelId) {
        def reel = reelService.loadReel(reelId)
        def user = userService.currentUser
        reel.audience.addToMembers(user)
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
        reel.audience.removeFromMembers(currentUser)
        reelService.storeReel(reel)
    }
}
