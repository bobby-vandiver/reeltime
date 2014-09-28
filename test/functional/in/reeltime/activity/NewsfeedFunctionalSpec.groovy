package in.reeltime.activity

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
        def request = requestFactory.newsfeed(token)

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
        newsfeed.activities.size() == 3

        and:
        newsfeed.activities[0].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == 'someone'

        newsfeed.activities[0].reel.reelId == reelId
        newsfeed.activities[0].reel.name == 'some reel'

        newsfeed.activities[0].video.videoId == additionalVideoId
        newsfeed.activities[0].video.title == 'another video'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'someone'

        newsfeed.activities[1].reel.reelId == reelId
        newsfeed.activities[1].reel.name == 'some reel'

        newsfeed.activities[1].video.videoId == videoId
        newsfeed.activities[1].video.title == 'some video'

        and:
        newsfeed.activities[2].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'someone'

        newsfeed.activities[2].reel.reelId == reelId
        newsfeed.activities[2].reel.name == 'some reel'
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
        newsfeed.activities.size() == 4

        and:
        newsfeed.activities[0].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == 'someone'

        newsfeed.activities[0].reel.reelId == someReelId
        newsfeed.activities[0].reel.name == 'some reel'

        newsfeed.activities[0].video.videoId == someVideoId
        newsfeed.activities[0].video.title == 'some video'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'anyone'

        newsfeed.activities[1].reel.reelId == anyReelId
        newsfeed.activities[1].reel.name == 'any reel'

        newsfeed.activities[1].video.videoId == anyVideoId
        newsfeed.activities[1].video.title == 'any video'

        and:
        newsfeed.activities[2].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'anyone'

        newsfeed.activities[2].reel.reelId == anyReelId
        newsfeed.activities[2].reel.name == 'any reel'

        and:
        newsfeed.activities[3].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'someone'

        newsfeed.activities[3].reel.reelId == someReelId
        newsfeed.activities[3].reel.name == 'some reel'
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

        newsfeed.activities[0].reel.reelId == uncategorizedReelId
        newsfeed.activities[0].reel.name == 'Uncategorized'

        newsfeed.activities[0].video.videoId == uncategorizedVideoId
        newsfeed.activities[0].video.title == 'uncategorized video'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'someone'

        newsfeed.activities[1].reel.reelId == reelId
        newsfeed.activities[1].reel.name == 'some reel'

        newsfeed.activities[1].video.videoId == additionalVideoId
        newsfeed.activities[1].video.title == 'another video'

        and:
        newsfeed.activities[2].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'someone'

        newsfeed.activities[2].reel.reelId == reelId
        newsfeed.activities[2].reel.name == 'some reel'

        newsfeed.activities[2].video.videoId == videoId
        newsfeed.activities[2].video.title == 'some video'

        and:
        newsfeed.activities[3].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'someone'

        newsfeed.activities[3].reel.reelId == reelId
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

        newsfeed.activities[0].reel.reelId == someReelId
        newsfeed.activities[0].reel.name == 'some reel'

        newsfeed.activities[0].video.videoId == someVideoId
        newsfeed.activities[0].video.title == 'some video'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'anyone'

        newsfeed.activities[1].reel.reelId == anyReelId
        newsfeed.activities[1].reel.name == 'any reel'

        newsfeed.activities[1].video.videoId == anyVideoId
        newsfeed.activities[1].video.title == 'any video'

        and:
        newsfeed.activities[2].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'anyone'

        newsfeed.activities[2].reel.reelId == anyReelId
        newsfeed.activities[2].reel.name == 'any reel'

        and:
        newsfeed.activities[3].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'someone'

        newsfeed.activities[3].reel.reelId == someReelId
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
        newsfeed.activities.size() == 4

        and:
        newsfeed.activities[0].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == 'someone'

        newsfeed.activities[0].reel.reelId == someReelId
        newsfeed.activities[0].reel.name == 'some reel'

        newsfeed.activities[0].video.videoId == someVideoId
        newsfeed.activities[0].video.title == 'some video'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'anyone'

        newsfeed.activities[1].reel.reelId == anyReelId
        newsfeed.activities[1].reel.name == 'any reel'

        newsfeed.activities[1].video.videoId == anyVideoId
        newsfeed.activities[1].video.title == 'any video'

        and:
        newsfeed.activities[2].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == 'anyone'

        newsfeed.activities[2].reel.reelId == anyReelId
        newsfeed.activities[2].reel.name == 'any reel'

        and:
        newsfeed.activities[3].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[3].user.username == 'someone'

        newsfeed.activities[3].reel.reelId == someReelId
        newsfeed.activities[3].reel.name == 'some reel'
    }

    void "user is following a user and one of the followed user's reels -- overlapping activities are only reported once"() {
        given:
        def someUserToken = registerNewUserAndGetToken('someone', ALL_SCOPES)
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(someUserToken, 'some video')

        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(someUserToken, 'someone')
        def reelId = reelTimeClient.addReel(someUserToken, 'some reel')

        and:
        reelTimeClient.addAudienceMember(testUserToken, reelId)
        reelTimeClient.followUser(testUserToken, 'someone')

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 2

        and:
        newsfeed.activities[0].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == 'someone'

        newsfeed.activities[0].reel.reelId == reelId
        newsfeed.activities[0].reel.name == 'some reel'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'someone'

        newsfeed.activities[1].reel.reelId == uncategorizedReelId
        newsfeed.activities[1].reel.name == 'Uncategorized'

        newsfeed.activities[1].video.videoId == videoId
        newsfeed.activities[1].video.title == 'some video'
    }
}
