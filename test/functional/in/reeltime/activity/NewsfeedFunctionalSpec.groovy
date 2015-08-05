package in.reeltime.activity

import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class NewsfeedFunctionalSpec extends FunctionalSpec {

    String testUserToken

    private static final String someone = 'someone'
    private static final String someReelName = 'some reel'
    private static final String someVideoTitle = 'some video'

    private static final String anyone = 'anyone'
    private static final String anyReelName = 'any reel'
    private static final String anyVideoTitle = 'any video'

    void setup() {
        testUserToken = getAccessTokenWithScopesForTestUser(ALL_SCOPES)
    }

    @Unroll
    void "invalid http methods for newsfeed endpoint"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.newsfeedUrl, ['post', 'put', 'delete'])
    }

    @Unroll
    void "invalid token scopes for newsfeed: #scopes"() {
        given:
        def token = getAccessTokenWithScopesForTestUser(scopes)
        def request = requestFactory.newsfeed(token, 1)

        when:
        def response = get(request)

        then:
        responseChecker.assertAuthJsonError(response, 403, 'access_denied', 'Access is denied')

        where:
        _   |   scopes
        _   |   ['users-read']
        _   |   ['audiences-read']
        _   |   ['users-read', 'audiences-write']
        _   |   ['users-write', 'audiences-read']
    }

    @Unroll
    void "invalid page [#page] requested"() {
        given:
        def request = requestFactory.newsfeed(testUserToken, page)

        when:
        def response = get(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, message)

        where:
        page    |   message
        -1      |   '[page] must be a positive number'
        0       |   '[page] must be a positive number'
        'abc'   |   '[page] is invalid'
    }

    void "new user should have no activity because they are not following anything"() {
        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.size() == 1
        newsfeed.activities.size() == 0
    }

    void "user is following a single reel"() {
        given:
        def someUserToken = registerNewUserAndGetToken(someone, ALL_SCOPES)

        def reelId = reelTimeClient.addReel(someUserToken, someReelName)
        def videoId = reelTimeClient.uploadVideoToReel(someUserToken, someReelName, someVideoTitle)

        def additionalVideoId = reelTimeClient.uploadVideoToReel(someUserToken, someReelName, 'another video')

        and:
        reelTimeClient.addReel(someUserToken, 'not followed')

        and:
        reelTimeClient.addAudienceMember(testUserToken, reelId)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        def activities = newsfeed.activities
        activities.size() == 4

        and:
        def joinSomeReel = findReelActivity(activities, [
                type: JOIN_REEL_AUDIENCE_ACTIVITY_TYPE, username: TEST_USER,
                reelId: reelId, reelName: someReelName
        ])

        def addAnotherVideoToSomeReel = findReelVideoActivity(activities, [
            type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
            reelId: reelId, reelName: someReelName,
            videoId: additionalVideoId, videoTitle: 'another video'
        ])

        def addSomeVideoToSomeReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
                reelId: reelId, reelName: someReelName,
                videoId: videoId, videoTitle: someVideoTitle
        ])

        def createSomeReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: someone,
                reelId: reelId, reelName: someReelName
        ])

        and:
        newsfeedContainsActivities(activities,
                [joinSomeReel, addAnotherVideoToSomeReel, addSomeVideoToSomeReel, createSomeReel])

        createReelAppearsChronologicallyBeforeDependentActivities(activities, createSomeReel,
                [joinSomeReel, addAnotherVideoToSomeReel, addSomeVideoToSomeReel])
    }

    void "user is following multiple reels"() {
        given:
        def someoneToken = registerNewUserAndGetToken(someone, ALL_SCOPES)
        def someReelId = reelTimeClient.addReel(someoneToken, someReelName)

        def anyoneToken = registerNewUserAndGetToken(anyone, ALL_SCOPES)
        def anyReelId = reelTimeClient.addReel(anyoneToken, anyReelName)

        def anyVideoId = reelTimeClient.uploadVideoToReel(anyoneToken, anyReelName, anyVideoTitle)
        def someVideoId = reelTimeClient.uploadVideoToReel(someoneToken, someReelName, someVideoTitle)

        and:
        reelTimeClient.addAudienceMember(testUserToken, someReelId)
        reelTimeClient.addAudienceMember(testUserToken, anyReelId)

        and:
        reelTimeClient.addReel(testUserToken, 'excludeUserReels')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        def activities = newsfeed.activities
        activities.size() == 6

        and:
        def joinAnyReel = findReelActivity(activities, [
                type: JOIN_REEL_AUDIENCE_ACTIVITY_TYPE, username: TEST_USER,
                reelId: anyReelId, reelName: anyReelName
        ])

        def joinSomeReel = findReelActivity(activities, [
                type: JOIN_REEL_AUDIENCE_ACTIVITY_TYPE, username: TEST_USER,
                reelId: someReelId, reelName: someReelName
        ])

        def addSomeVideoToSomeReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
                reelId: someReelId, reelName: someReelName,
                videoId: someVideoId, videoTitle: someVideoTitle
        ])

        def addAnyVideoToAnyReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: anyone,
                reelId: anyReelId, reelName: anyReelName,
                videoId: anyVideoId, videoTitle: anyVideoTitle
        ])

        def createAnyReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: anyone,
                reelId: anyReelId, reelName: anyReelName
        ])

        def createSomeReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: someone,
                reelId: someReelId, reelName: someReelName
        ])

        and:
        newsfeedContainsActivities(activities,
                [joinAnyReel, joinSomeReel, addSomeVideoToSomeReel, addAnyVideoToAnyReel, createAnyReel, createSomeReel])

        and:
        createReelAppearsChronologicallyBeforeDependentActivities(activities, createAnyReel,
                [joinAnyReel, addAnyVideoToAnyReel])

        createReelAppearsChronologicallyBeforeDependentActivities(activities, createSomeReel,
                [joinSomeReel, addSomeVideoToSomeReel])
    }

    void "user is following a single user"() {
        given:
        def someUserToken = registerNewUserAndGetToken(someone, ALL_SCOPES)

        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(someUserToken, someone)
        def reelId = reelTimeClient.addReel(someUserToken, someReelName)

        def videoId = reelTimeClient.uploadVideoToReel(someUserToken, someReelName, someVideoTitle)
        def additionalVideoId = reelTimeClient.uploadVideoToReel(someUserToken, someReelName, 'another video')

        def uncategorizedVideoId = reelTimeClient.uploadVideoToUncategorizedReel(someUserToken, 'uncategorized video')

        and:
        registerNewUserAndGetToken('nobody', ALL_SCOPES)

        and:
        reelTimeClient.followUser(testUserToken, someone)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        def activities = newsfeed.activities
        activities.size() == 4

        and:
        def addUncategorizedVideoToUncategorizedReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
                reelId: uncategorizedReelId, reelName: 'Uncategorized',
                videoId: uncategorizedVideoId, videoTitle: 'uncategorized video'
        ])

        def addAnotherVideoToSomeReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
                reelId: reelId, reelName: someReelName,
                videoId: additionalVideoId, videoTitle: 'another video'
        ])

        def addSomeVideoToSomeReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
                reelId: reelId, reelName: someReelName,
                videoId: videoId, videoTitle: someVideoTitle
        ])

        def createSomeReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: someone,
                reelId: reelId, reelName: someReelName
        ])

        and:
        newsfeedContainsActivities(activities,
                [addUncategorizedVideoToUncategorizedReel, addAnotherVideoToSomeReel, addSomeVideoToSomeReel, createSomeReel])

        createReelAppearsChronologicallyBeforeDependentActivities(activities, createSomeReel,
                [addUncategorizedVideoToUncategorizedReel, addAnotherVideoToSomeReel, addSomeVideoToSomeReel])
    }

    void "user is following multiple users"() {
        given:
        def someoneToken = registerNewUserAndGetToken(someone, ALL_SCOPES)
        def someReelId = reelTimeClient.addReel(someoneToken, someReelName)

        def anyoneToken = registerNewUserAndGetToken(anyone, ALL_SCOPES)
        def anyReelId = reelTimeClient.addReel(anyoneToken, anyReelName)

        def anyVideoId = reelTimeClient.uploadVideoToReel(anyoneToken, anyReelName, anyVideoTitle)
        def someVideoId = reelTimeClient.uploadVideoToReel(someoneToken, someReelName, someVideoTitle)

        and:
        reelTimeClient.followUser(testUserToken, someone)
        reelTimeClient.followUser(testUserToken, anyone)

        and:
        reelTimeClient.addReel(testUserToken, 'excludeUserReels')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        def activities = newsfeed.activities
        activities.size() == 4

        and:
        def addSomeVideoToSomeReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
                reelId: someReelId, reelName: someReelName,
                videoId: someVideoId, videoTitle: someVideoTitle
        ])

        def addAnyVideoToAnyReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: anyone,
                reelId: anyReelId, reelName: anyReelName,
                videoId: anyVideoId, videoTitle: anyVideoTitle
        ])

        def createAnyReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: anyone,
                reelId: anyReelId, reelName: anyReelName
        ])

        def createSomeReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: someone,
                reelId: someReelId, reelName: someReelName
        ])

        and:
        newsfeedContainsActivities(activities,
                [addSomeVideoToSomeReel, addAnyVideoToAnyReel, createAnyReel, createSomeReel])

        and:
        createReelAppearsChronologicallyBeforeDependentActivities(activities, createAnyReel, [addAnyVideoToAnyReel])
        createReelAppearsChronologicallyBeforeDependentActivities(activities, createSomeReel, [addSomeVideoToSomeReel])
    }

    void "user is following a mix of users and reels"() {
        given:
        def someoneToken = registerNewUserAndGetToken(someone, ALL_SCOPES)
        def someReelId = reelTimeClient.addReel(someoneToken, someReelName)

        def anyoneToken = registerNewUserAndGetToken(anyone, ALL_SCOPES)
        def anyReelId = reelTimeClient.addReel(anyoneToken, anyReelName)

        def anyVideoId = reelTimeClient.uploadVideoToReel(anyoneToken, anyReelName, anyVideoTitle)
        def someVideoId = reelTimeClient.uploadVideoToReel(someoneToken, someReelName, someVideoTitle)

        and:
        reelTimeClient.addAudienceMember(testUserToken, someReelId)
        reelTimeClient.followUser(testUserToken, anyone)

        and:
        reelTimeClient.addReel(testUserToken, 'excludeUserReels')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        def activities = newsfeed.activities
        activities.size() == 5

        and:
        def joinSomeReel = findReelActivity(activities, [
                type: JOIN_REEL_AUDIENCE_ACTIVITY_TYPE, username: TEST_USER,
                reelId: someReelId, reelName: someReelName
        ])

        def addSomeVideoToSomeReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
                reelId: someReelId, reelName: someReelName,
                videoId: someVideoId, videoTitle: someVideoTitle
        ])

        def addAnyVideoToAnyReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: anyone,
                reelId: anyReelId, reelName: anyReelName,
                videoId: anyVideoId, videoTitle: anyVideoTitle
        ])

        def createAnyReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: anyone,
                reelId: anyReelId, reelName: anyReelName
        ])

        def createSomeReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: someone,
                reelId: someReelId, reelName: someReelName
        ])

        and:
        newsfeedContainsActivities(activities,
                [joinSomeReel, addSomeVideoToSomeReel,addAnyVideoToAnyReel, createAnyReel, createSomeReel])

        and:
        createReelAppearsChronologicallyBeforeDependentActivities(activities, createAnyReel,
                [addAnyVideoToAnyReel])

        createReelAppearsChronologicallyBeforeDependentActivities(activities, createSomeReel,
                [joinSomeReel, addSomeVideoToSomeReel])
    }

    void "user is following a user who hasn't done anything"() {
        given:
        registerNewUserAndGetToken(someone, ALL_SCOPES)
        reelTimeClient.followUser(testUserToken, someone)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 0
    }

    void "user is not following anyone or any reels but others are"() {
        given:
        def someUserToken = registerNewUserAndGetToken(someone, ALL_SCOPES)
        def anyUserToken = registerNewUserAndGetToken(anyone, ALL_SCOPES)

        and:
        def reelId = reelTimeClient.addReel(someUserToken, someReelName)

        and:
        reelTimeClient.followUser(someUserToken, anyone)
        reelTimeClient.addAudienceMember(anyUserToken, reelId)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 0
    }

    void "user is following a user and one of the followed user's reels -- overlapping activities are only reported once"() {
        given:
        def someUserToken = registerNewUserAndGetToken(someone, ALL_SCOPES)
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(someUserToken, someVideoTitle)

        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(someUserToken, someone)
        def reelId = reelTimeClient.addReel(someUserToken, someReelName)

        and:
        def nobodyToken = registerNewUserAndGetToken('nobody', ALL_SCOPES)
        reelTimeClient.addReel(nobodyToken, 'excluded')

        and:
        reelTimeClient.addAudienceMember(testUserToken, reelId)
        reelTimeClient.followUser(testUserToken, someone)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        def activities = newsfeed.activities
        activities.size() == 3

        and:
        def joinSomeReel = findReelActivity(activities, [
                type: JOIN_REEL_AUDIENCE_ACTIVITY_TYPE, username: TEST_USER,
                reelId: reelId, reelName: someReelName
        ])

        def createSomeReel = findReelActivity(activities, [
                type: CREATE_REEL_ACTIVITY_TYPE, username: someone,
                reelId: reelId, reelName: someReelName
        ])

        def addSomeVideoToUncategorizedReel = findReelVideoActivity(activities, [
                type: ADD_VIDEO_TO_REEL_ACTIVITY_TYPE, username: someone,
                reelId: uncategorizedReelId, reelName: 'Uncategorized',
                videoId: videoId, videoTitle: someVideoTitle
        ])

        and:
        newsfeedContainsActivities(activities,
                [joinSomeReel, createSomeReel, addSomeVideoToUncategorizedReel])

        createReelAppearsChronologicallyBeforeDependentActivities(activities, createSomeReel, [joinSomeReel])
    }

    private Object findReelActivity(activities, criteria) {
        def activity = activities.find {
            it.type == criteria.type && it.user.username == criteria.username &&
                    it.reel.reel_id == criteria.reelId && it.reel.name == criteria.reelName
        }
        assert activity != null
        return activity
    }

    private Object findReelVideoActivity(activities, criteria) {
        def activity = activities.find {
            it.type == criteria.type && it.user.username == criteria.username &&
                    it.reel.reel_id == criteria.reelId && it.reel.name == criteria.reelName &&
                    it.video.video_id == criteria.videoId && it.video.title == criteria.videoTitle
        }
        assert activity != null
        return activity
    }

    private void newsfeedContainsActivities(newsfeedActivities, activities) {
        activities.each {
            assert newsfeedActivities.contains(it)
        }
    }

    private void createReelAppearsChronologicallyBeforeDependentActivities(newsfeedActivities, createReelActivity, dependentActivities) {
        def createReelPosition = newsfeedActivities.indexOf(createReelActivity)

        dependentActivities.each { activity ->
            def position = newsfeedActivities.indexOf(activity)
            assert position < createReelPosition : "create reel: $createReelActivity\nactivity: $activity"
        }
    }
}
