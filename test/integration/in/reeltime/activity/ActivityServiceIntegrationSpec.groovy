package in.reeltime.activity

import grails.test.spock.IntegrationSpec
import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video
import spock.lang.Unroll
import test.helper.UserFactory

class ActivityServiceIntegrationSpec extends IntegrationSpec {

    def activityService

    User user
    Reel reel
    Video video

    static final int TEST_MAX_ACTIVITIES_PER_PAGE = 3
    int savedMaxActivitiesPerPage

    void setup() {
        user = UserFactory.createTestUser()
        reel = user.reels[0]
        video = new Video(creator: user, title: 'title', masterPath: 'masterPath', available: true).save()

        savedMaxActivitiesPerPage = activityService.maxActivitiesPerPage
        activityService.maxActivitiesPerPage = TEST_MAX_ACTIVITIES_PER_PAGE
    }

    void cleanup() {
        activityService.maxActivitiesPerPage = savedMaxActivitiesPerPage
    }

    void "save reel creation activity"() {
        when:
        activityService.reelCreated(user, reel)

        then:
        def activity = UserReelActivity.findByUserAndReel(user, reel)
        activity != null

        activity instanceof CreateReelActivity
        activity.type == ActivityType.CreateReel
    }

    void "attempt to add real creation activity multiple times"() {
        given:
        activityService.reelCreated(user, reel)

        when:
        activityService.reelCreated(user, reel)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Reel creation activity already exists"
    }

    void "save video added to reel activity"() {
        when:
        activityService.videoAddedToReel(user, reel, video)

        then:
        def activity = UserReelActivity.findByUserAndReel(user, reel)
        activity != null

        activity instanceof AddVideoToReelActivity
        activity.type == ActivityType.AddVideoToReel
    }

    void "attempt to add video added to reel activity multiple times"() {
        given:
        activityService.videoAddedToReel(user, reel, video)

        when:
        activityService.videoAddedToReel(user, reel, video)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Video added to reel activity already exists"
    }

    void "no user activities to delete"() {
        when:
        activityService.deleteAllUserActivity(user)

        then:
        notThrown(Exception)
    }

    void "delete all user activity"() {
        given:
        def activity1 = new CreateReelActivity(user: user, reel: reel).save()
        def activity2 = new CreateReelActivity(user: user, reel: reel).save()

        and:
        def activityId1 = activity1.id
        assert UserReelActivity.findById(activityId1) != null

        and:
        def activityId2 = activity2.id
        assert UserReelActivity.findById(activityId2) != null

        when:
        activityService.deleteAllUserActivity(user)

        then:
        UserReelActivity.findById(activityId1) == null
        UserReelActivity.findById(activityId2) == null
    }

    void "empty criteria for activities"() {
        when:
        def list = activityService.findActivities([], [])

        then:
        list.size() == 0
    }

    void "no activities matching criteria"() {
        when:
        def list = activityService.findActivities([user], [reel])

        then:
        list.size() == 0
    }

    void "list activity by user and reel returns mixed list"() {
        given:
        activityService.reelCreated(user, reel)
        activityService.videoAddedToReel(user, reel, video)

        expect:
        assertFindActivities([user], [reel])
        assertFindActivities([user], [])
        assertFindActivities([], [reel])
    }

    private void assertFindActivities(List<User> users, List<Reel> reels) {
        def list = activityService.findActivities(users, reels)
        assert list.size() == 2

        assert list[0].type == ActivityType.AddVideoToReel
        assert list[1].type == ActivityType.CreateReel
    }

    void "list first page of activity if no page is specified"() {
        given:
        def secondPage = createActivityPage()
        def firstPage = createActivityPage()

        when:
        def list = activityService.findActivities([user], [reel])

        then:
        list.size() == TEST_MAX_ACTIVITIES_PER_PAGE

        and:
        list == firstPage
        list != secondPage
    }

    void "specify the page of activity to list"() {
        given:
        def thirdPage = createActivityPage()
        def secondPage = createActivityPage()
        def firstPage = createActivityPage()

        when:
        def list = activityService.findActivities([user], [reel], 2)

        then:
        list.size() == TEST_MAX_ACTIVITIES_PER_PAGE

        and:
        list != firstPage
        list == secondPage
        list != thirdPage
    }

    private List<UserReelActivity> createActivityPage() {
        List<UserReelActivity> activities = []
        TEST_MAX_ACTIVITIES_PER_PAGE.times {
            activities << new CreateReelActivity(user: user, reel: reel).save(validate: false)
            sleep(2 * 1000)
        }
        activities.sort { a, b -> b.dateCreated <=> a.dateCreated }
        return activities
    }
}