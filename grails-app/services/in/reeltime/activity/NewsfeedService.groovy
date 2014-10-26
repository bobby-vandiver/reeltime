package in.reeltime.activity

class NewsfeedService {

    def activityService
    def audienceService

    def userFollowingService
    def authenticationService

    private static final MIN_PAGE_NUMBER = 1

    List<UserReelActivity> listRecentActivity(int pageNumber) {
        validatePageNumber(pageNumber)
        def currentUser = authenticationService.currentUser

        def reelsFollowed = audienceService.listReelsForAudienceMember(currentUser)
        def usersFollowed = userFollowingService.listAllFolloweesForFollower(currentUser)

        return activityService.findActivities(usersFollowed, reelsFollowed, pageNumber)
    }

    private static void validatePageNumber(int pageNumber) {
        if(pageNumber < MIN_PAGE_NUMBER) {
            throw new IllegalArgumentException("Page number must be $MIN_PAGE_NUMBER or greater")
        }
    }
}
