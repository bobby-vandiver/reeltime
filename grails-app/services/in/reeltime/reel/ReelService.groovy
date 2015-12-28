package in.reeltime.reel

import in.reeltime.exceptions.ReelNotFoundException

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
        def user = userService.loadUser(username)
        def params = paginationParams(page)

        def reelIds = UserReel.withCriteria {
            eq('owner', user)
            projections {
                property('reel.id')
            }
        } as List<Long>

        Reel.findAllByIdInList(reelIds, params)
    }

    private paginationParams(int page) {
        int offset = (page - 1) * maxReelsPerPage
        [max: maxReelsPerPage as int, offset: offset, sort: 'dateCreated', order: 'desc']
    }
}
