package in.reeltime.activity

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class NewsfeedFunctionalSpec extends FunctionalSpec {

    String testUserToken

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
        def someUserToken = registerNewUserAndGetToken('someone', ALL_SCOPES)

        def reelId = reelTimeClient.addReel(someUserToken, 'some reel')
        def videoId = reelTimeClient.uploadVideoToReel(someUserToken, 'some reel', 'some video')

        def additionalVideoId = reelTimeClient.uploadVideoToReel(someUserToken, 'some reel', 'another video')

        and:
        reelTimeClient.addReel(someUserToken, 'not followed')

        and:
        reelTimeClient.addAudienceMember(testUserToken, reelId)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 4

        and:
        newsfeed.activities[0].type == JOIN_REEL_AUDIENCE_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == TEST_USER

        newsfeed.activities[0].reel.reel_id == reelId
        newsfeed.activities[0].reel.name == 'some reel'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'someone'

        newsfeed.activities[1].reel.reel_id == reelId
        newsfeed.activities[1].reel.name == 'some reel'

        newsfeed.activities[1].video.videoId == additionalVideoId
        newsfeed.activities[1].video.title == 'another video'

        and:
        newsfeed.activities[2].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'someone'

        newsfeed.activities[2].reel.reel_id == reelId
        newsfeed.activities[2].reel.name == 'some reel'

        newsfeed.activities[2].video.videoId == videoId
        newsfeed.activities[2].video.title == 'some video'

        and:
        newsfeed.activities[3].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'someone'

        newsfeed.activities[3].reel.reel_id == reelId
        newsfeed.activities[3].reel.name == 'some reel'
    }

    void "user is following multiple reels"() {
        given:
        def someoneToken = registerNewUserAndGetToken('someone', ALL_SCOPES)
        def someReelId = reelTimeClient.addReel(someoneToken, 'some reel')

        def anyoneToken = registerNewUserAndGetToken('anyone', ALL_SCOPES)
        def anyReelId = reelTimeClient.addReel(anyoneToken, 'any reel')

        def anyVideoId = reelTimeClient.uploadVideoToReel(anyoneToken, 'any reel', 'any video')
        def someVideoId = reelTimeClient.uploadVideoToReel(someoneToken, 'some reel', 'some video')

        and:
        reelTimeClient.addAudienceMember(testUserToken, someReelId)
        reelTimeClient.addAudienceMember(testUserToken, anyReelId)

        and:
        reelTimeClient.addReel(testUserToken, 'excludeUserReels')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 6

        and:
        newsfeed.activities[0].type == JOIN_REEL_AUDIENCE_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == TEST_USER

        newsfeed.activities[0].reel.reel_id == anyReelId
        newsfeed.activities[0].reel.name == 'any reel'

        and:
        newsfeed.activities[1].type == JOIN_REEL_AUDIENCE_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == TEST_USER

        newsfeed.activities[1].reel.reel_id == someReelId
        newsfeed.activities[1].reel.name == 'some reel'

        and:
        newsfeed.activities[2].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'someone'

        newsfeed.activities[2].reel.reel_id == someReelId
        newsfeed.activities[2].reel.name == 'some reel'

        newsfeed.activities[2].video.videoId == someVideoId
        newsfeed.activities[2].video.title == 'some video'

        and:
        newsfeed.activities[3].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'anyone'

        newsfeed.activities[3].reel.reel_id == anyReelId
        newsfeed.activities[3].reel.name == 'any reel'

        newsfeed.activities[3].video.videoId == anyVideoId
        newsfeed.activities[3].video.title == 'any video'

        and:
        newsfeed.activities[4].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[4].user.username == 'anyone'

        newsfeed.activities[4].reel.reel_id == anyReelId
        newsfeed.activities[4].reel.name == 'any reel'

        and:
        newsfeed.activities[5].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[5].user.username == 'someone'

        newsfeed.activities[5].reel.reel_id == someReelId
        newsfeed.activities[5].reel.name == 'some reel'
    }

    void "user is following a single user"() {
        given:
        def someUserToken = registerNewUserAndGetToken('someone', ALL_SCOPES)

        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(someUserToken, 'someone')
        def reelId = reelTimeClient.addReel(someUserToken, 'some reel')

        def videoId = reelTimeClient.uploadVideoToReel(someUserToken, 'some reel', 'some video')
        def additionalVideoId = reelTimeClient.uploadVideoToReel(someUserToken, 'some reel', 'another video')

        def uncategorizedVideoId = reelTimeClient.uploadVideoToUncategorizedReel(someUserToken, 'uncategorized video')

        and:
        registerNewUserAndGetToken('nobody', ALL_SCOPES)

        and:
        reelTimeClient.followUser(testUserToken, 'someone')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 4

        and:
        newsfeed.activities[0].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == 'someone'

        newsfeed.activities[0].reel.reel_id == uncategorizedReelId
        newsfeed.activities[0].reel.name == 'Uncategorized'

        newsfeed.activities[0].video.videoId == uncategorizedVideoId
        newsfeed.activities[0].video.title == 'uncategorized video'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'someone'

        newsfeed.activities[1].reel.reel_id == reelId
        newsfeed.activities[1].reel.name == 'some reel'

        newsfeed.activities[1].video.videoId == additionalVideoId
        newsfeed.activities[1].video.title == 'another video'

        and:
        newsfeed.activities[2].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'someone'

        newsfeed.activities[2].reel.reel_id == reelId
        newsfeed.activities[2].reel.name == 'some reel'

        newsfeed.activities[2].video.videoId == videoId
        newsfeed.activities[2].video.title == 'some video'

        and:
        newsfeed.activities[3].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'someone'

        newsfeed.activities[3].reel.reel_id == reelId
        newsfeed.activities[3].reel.name == 'some reel'
    }

    void "user is following multiple users"() {
        given:
        def someoneToken = registerNewUserAndGetToken('someone', ALL_SCOPES)
        def someReelId = reelTimeClient.addReel(someoneToken, 'some reel')

        def anyoneToken = registerNewUserAndGetToken('anyone', ALL_SCOPES)
        def anyReelId = reelTimeClient.addReel(anyoneToken, 'any reel')

        def anyVideoId = reelTimeClient.uploadVideoToReel(anyoneToken, 'any reel', 'any video')
        def someVideoId = reelTimeClient.uploadVideoToReel(someoneToken, 'some reel', 'some video')

        and:
        reelTimeClient.followUser(testUserToken, 'someone')
        reelTimeClient.followUser(testUserToken, 'anyone')

        and:
        reelTimeClient.addReel(testUserToken, 'excludeUserReels')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 4

        and:
        newsfeed.activities[0].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == 'someone'

        newsfeed.activities[0].reel.reel_id == someReelId
        newsfeed.activities[0].reel.name == 'some reel'

        newsfeed.activities[0].video.videoId == someVideoId
        newsfeed.activities[0].video.title == 'some video'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'anyone'

        newsfeed.activities[1].reel.reel_id == anyReelId
        newsfeed.activities[1].reel.name == 'any reel'

        newsfeed.activities[1].video.videoId == anyVideoId
        newsfeed.activities[1].video.title == 'any video'

        and:
        newsfeed.activities[2].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'anyone'

        newsfeed.activities[2].reel.reel_id == anyReelId
        newsfeed.activities[2].reel.name == 'any reel'

        and:
        newsfeed.activities[3].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'someone'

        newsfeed.activities[3].reel.reel_id == someReelId
        newsfeed.activities[3].reel.name == 'some reel'
    }

    void "user is following a mix of users and reels"() {
        given:
        def someoneToken = registerNewUserAndGetToken('someone', ALL_SCOPES)
        def someReelId = reelTimeClient.addReel(someoneToken, 'some reel')

        def anyoneToken = registerNewUserAndGetToken('anyone', ALL_SCOPES)
        def anyReelId = reelTimeClient.addReel(anyoneToken, 'any reel')

        def anyVideoId = reelTimeClient.uploadVideoToReel(anyoneToken, 'any reel', 'any video')
        def someVideoId = reelTimeClient.uploadVideoToReel(someoneToken, 'some reel', 'some video')

        and:
        reelTimeClient.addAudienceMember(testUserToken, someReelId)
        reelTimeClient.followUser(testUserToken, 'anyone')

        and:
        reelTimeClient.addReel(testUserToken, 'excludeUserReels')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:


        newsfeed.activities.size() == 5

        and:
        newsfeed.activities[0].type == JOIN_REEL_AUDIENCE_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == TEST_USER

        newsfeed.activities[0].reel.reel_id == someReelId
        newsfeed.activities[0].reel.name == 'some reel'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'someone'

        newsfeed.activities[1].reel.reel_id == someReelId
        newsfeed.activities[1].reel.name == 'some reel'

        newsfeed.activities[1].video.videoId == someVideoId
        newsfeed.activities[1].video.title == 'some video'

        and:
        newsfeed.activities[2].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'anyone'

        newsfeed.activities[2].reel.reel_id == anyReelId
        newsfeed.activities[2].reel.name == 'any reel'

        newsfeed.activities[2].video.videoId == anyVideoId
        newsfeed.activities[2].video.title == 'any video'

        and:
        newsfeed.activities[3].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'anyone'

        newsfeed.activities[3].reel.reel_id == anyReelId
        newsfeed.activities[3].reel.name == 'any reel'

        and:
        newsfeed.activities[4].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[4].user.username == 'someone'

        newsfeed.activities[4].reel.reel_id == someReelId
        newsfeed.activities[4].reel.name == 'some reel'
    }

    void "user is following a user who hasn't done anything"() {
        given:
        registerNewUserAndGetToken('someone', ALL_SCOPES)
        reelTimeClient.followUser(testUserToken, 'someone')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 0
    }

    void "user is following a user and one of the followed user's reels -- overlapping activities are only reported once"() {
        given:
        def someUserToken = registerNewUserAndGetToken('someone', ALL_SCOPES)
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(someUserToken, 'some video')

        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(someUserToken, 'someone')
        def reelId = reelTimeClient.addReel(someUserToken, 'some reel')

        and:
        def nobodyToken = registerNewUserAndGetToken('nobody', ALL_SCOPES)
        reelTimeClient.addReel(nobodyToken, 'excluded')

        and:
        reelTimeClient.addAudienceMember(testUserToken, reelId)
        reelTimeClient.followUser(testUserToken, 'someone')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 3

        and:
        newsfeed.activities[0].type == JOIN_REEL_AUDIENCE_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == TEST_USER

        newsfeed.activities[0].reel.reel_id == reelId
        newsfeed.activities[0].reel.name == 'some reel'

        and:
        newsfeed.activities[1].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'someone'

        newsfeed.activities[1].reel.reel_id == reelId
        newsfeed.activities[1].reel.name == 'some reel'

        and:
        newsfeed.activities[2].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'someone'

        newsfeed.activities[2].reel.reel_id == uncategorizedReelId
        newsfeed.activities[2].reel.name == 'Uncategorized'

        newsfeed.activities[2].video.videoId == videoId
        newsfeed.activities[2].video.title == 'some video'
    }
}
