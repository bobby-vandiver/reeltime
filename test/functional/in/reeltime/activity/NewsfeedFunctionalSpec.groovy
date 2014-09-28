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
        def username = 'username'
        def reelName = 'some reel'
        def videoTitle = 'some video'

        and:
        def someUserToken = registerNewUserAndGetToken(username, ALL_SCOPES)

        def reelId = reelTimeClient.addReel(someUserToken, reelName)
        def videoId = reelTimeClient.uploadVideoToReel(someUserToken, reelName, videoTitle)

        def additionalVideoTitle = 'another video'
        def additionalVideoId = reelTimeClient.uploadVideoToReel(someUserToken, reelName, additionalVideoTitle)

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
        newsfeed.activities[0].user.username == username

        newsfeed.activities[0].reel.reelId == reelId
        newsfeed.activities[0].reel.name == reelName

        newsfeed.activities[0].video.videoId == additionalVideoId
        newsfeed.activities[0].video.title == additionalVideoTitle

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == username

        newsfeed.activities[1].reel.reelId == reelId
        newsfeed.activities[1].reel.name == reelName

        newsfeed.activities[1].video.videoId == videoId
        newsfeed.activities[1].video.title == videoTitle

        and:
        newsfeed.activities[2].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[2].user.username == username

        newsfeed.activities[2].reel.reelId == reelId
        newsfeed.activities[2].reel.name == reelName
    }

    void "user is following multiple reels"() {

    }

    void "user is following a single user"() {

    }

    void "user is following multiple users"() {

    }

    void "user is following a mix of users and reels"() {
    }

    void "user is following a user and one of the followed user's reels -- overlapping activities are only reported once"() {
        given:
        def username = 'username'
        def reelName = 'some reel'
        def videoTitle = 'some video'

        and:
        def someUserToken = registerNewUserAndGetToken(username, ALL_SCOPES)
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(someUserToken, videoTitle)

        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(someUserToken, username)
        def reelId = reelTimeClient.addReel(someUserToken, reelName)

        and:
        reelTimeClient.addAudienceMember(testUserToken, reelId)
        reelTimeClient.followUser(testUserToken, username)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 2

        and:
        newsfeed.activities[0].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == username

        newsfeed.activities[0].reel.reelId == reelId
        newsfeed.activities[0].reel.name == reelName

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == username

        newsfeed.activities[1].reel.reelId == uncategorizedReelId
        newsfeed.activities[1].reel.name == 'Uncategorized'

        newsfeed.activities[1].video.videoId == videoId
        newsfeed.activities[1].video.title == videoTitle
    }
}
