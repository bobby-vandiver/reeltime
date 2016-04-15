package in.reeltime.reel

import grails.transaction.Transactional
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User

@Transactional
class AudienceService {

    def reelService
    def reelAuthorizationService

    def authenticationService
    def activityService

    def maxMembersPerPage

    List<User> listMembers(Long reelId, int page) {
        def reel = reelService.loadReel(reelId)

        def memberIds = AudienceMember.withCriteria {
            eq('reel', reel)
            projections {
                property('member.id')
            }
        } as List<Long>

        User.findAllByIdInListInAlphabeticalOrderByPage(memberIds, page, maxMembersPerPage)
    }

    List<Reel> listReelsForAudienceMember(User member) {
        AudienceMember.findAllByMember(member)*.reel
    }

    void addCurrentUserToAudience(Long reelId) {
        def reel = reelService.loadReel(reelId)

        if(reelAuthorizationService.currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Owner of a reel cannot be a member of the reel's audience")
        }

        def currentUser = authenticationService.currentUser
        new AudienceMember(reel: reel, member: currentUser).save()

        activityService.userJoinedAudience(currentUser, reel)
    }

    void removeCurrentUserFromAudience(Long reelId) {
        def reel = reelService.loadReel(reelId)
        def audience = reel.audience

        def currentUser = authenticationService.currentUser
        if(!audience.contains(currentUser)) {
            def message = "Current user [${currentUser.username}] is not a member of the audience for reel [$reelId]"
            throw new AuthorizationException(message)
        }

        def membership = AudienceMember.findByReelAndMember(reel, currentUser)
        removeAudienceMembership(membership)
    }

    void removeMemberFromAllAudiences(User member) {
        def memberships = AudienceMember.findAllByMember(member)
        removeAllAudienceMemberships(memberships)
    }

    void removeAllMembersFromAudience(Reel reel) {
        def memberships = AudienceMember.findAllByReel(reel)
        removeAllAudienceMemberships(memberships)
    }

    private void removeAllAudienceMemberships(Collection<AudienceMember> memberships) {
        def list = []
        list.addAll(memberships)

        list.each { AudienceMember membership ->
            removeAudienceMembership(membership)
        }
    }

    private void removeAudienceMembership(AudienceMember membership) {
        membership.delete()
        activityService.userLeftAudience(membership.member, membership.reel)
    }
}
