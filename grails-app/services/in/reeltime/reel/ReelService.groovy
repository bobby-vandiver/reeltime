package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.user.User

class ReelService {

    def userService
    def authenticationService

    def reelAuthorizationService
    def activityService

    def maxReelsPerPage

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

    List<Reel> listReels(int page) {
        Reel.list(paginationParams(page))
    }

    List<Reel> listReelsByUsername(String username, int page) {
        def reelIds = userService.loadUser(username).reels.toList().collect { it.id }
        Reel.findAllByIdInList(reelIds, paginationParams(page))
    }

    private paginationParams(int page) {
        int offset = (page - 1) * maxReelsPerPage
        [max: maxReelsPerPage, offset: offset, sort: 'dateCreated', order: 'desc']
    }
}
