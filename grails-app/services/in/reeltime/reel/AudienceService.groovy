package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User


class AudienceService {

    def reelService
    def reelAuthorizationService

    def userAuthenticationService
    def activityService

    Collection<User> listMembers(Long reelId) {
        def reel = reelService.loadReel(reelId)
        return reel.audience.members
    }

    List<Reel> listReelsForAudienceMember(User member) {
        def audiences = Audience.findAllByAudienceMember(member)
        return audiences*.reel
    }

    void addCurrentUserToAudience(Long reelId) {
        def reel = reelService.loadReel(reelId)
        if(reelAuthorizationService.currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Owner of a reel cannot be a member of the reel's audience")
        }
        def currentUser = userAuthenticationService.currentUser
        def audience = reel.audience

        audience.addToMembers(currentUser)
        storeAudience(audience)

        activityService.userJoinedAudience(currentUser, audience)
    }

    void removeCurrentUserFromAudience(Long reelId) {
        def reel = reelService.loadReel(reelId)
        def audience = reel.audience

        def currentUser = userAuthenticationService.currentUser
        if(!audience.hasMember(currentUser)) {
            def message = "Current user [${currentUser.username}] is not a member of the audience for reel [$reelId]"
            throw new AuthorizationException(message)
        }
        removeMemberFromAudience(currentUser, audience)
    }

    void removeMemberFromAllAudiences(User member) {
        def audiences = Audience.findAllByAudienceMember(member)

        audiences.each { audience ->
            removeMemberFromAudience(member, audience)
        }
    }

    private void removeMemberFromAudience(User member, Audience audience) {
        audience.removeFromMembers(member)
        storeAudience(audience)
        activityService.userLeftAudience(member, audience)
    }

    void storeAudience(Audience audience) {
        audience.save()
    }
}
