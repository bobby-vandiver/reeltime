package in.reeltime.reel

import in.reeltime.user.User


class AudienceService {

    def reelService

    Collection<User> listMembers(Long reelId) {
        def reel = reelService.loadReel(reelId)
        return reel.audience.members
    }
}
