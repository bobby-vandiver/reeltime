package in.reeltime.activity

import grails.test.mixin.TestFor
import in.reeltime.reel.AudienceService
import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.user.UserAuthenticationService
import in.reeltime.user.UserFollowingService
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(NewsfeedService)
class NewsfeedServiceSpec extends Specification {

    ActivityService activityService
    AudienceService audienceService

    UserFollowingService userFollowingService
    UserAuthenticationService userAuthenticationService

    User follower
    Reel reel

    User followee
    UserReelActivity activity

    void setup() {
        follower = Stub(User)
        reel = Stub(Reel)

        followee = Stub(User)
        activity = Stub(UserReelActivity)

        assert follower != followee

        activityService = Mock(ActivityService)
        audienceService = Mock(AudienceService)
        userFollowingService = Mock(UserFollowingService)

        userAuthenticationService = Stub(UserAuthenticationService) {
            getCurrentUser() >> follower
        }

        service.activityService = activityService
        service.audienceService = audienceService
        service.userFollowingService = userFollowingService
        service.userAuthenticationService = userAuthenticationService
    }

    @Unroll
    void "request page [#pageNumber] of recent activities of users and reel the current user is following"() {
        when:
        def list = service.listRecentActivity(pageNumber)

        then:
        list == [activity]

        and:
        1 * audienceService.listReelsForAudienceMember(follower) >> [reel]
        1 * userFollowingService.listFolloweesForFollower(follower) >> [followee]

        and:
        1 * activityService.findActivities([followee], [reel], pageNumber) >> [activity]

        where:
        _   |   pageNumber
        _   |   1
        _   |   4
        _   |   19
    }

    @Unroll
    void "page numbering begins at 1 -- [#pageNumber] is not allowed"() {
        when:
        service.listRecentActivity(pageNumber)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Page number must be 1 or greater"

        where:
        _   |   pageNumber
        _   |   0
        _   |   -1
        _   |   -1234515
    }
}
