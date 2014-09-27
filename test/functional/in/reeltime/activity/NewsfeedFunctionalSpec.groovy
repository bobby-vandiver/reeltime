package in.reeltime.activity

import in.reeltime.FunctionalSpec
import spock.lang.Ignore
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

    void "new user should have no activity because they are not following anything"() {
        given:
        def someUserToken = registerNewUserAndGetToken('someone', ALL_SCOPES)
        reelTimeClient.uploadVideo(someUserToken)
        reelTimeClient.addReel('some reel', someUserToken)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.size() == 1
        newsfeed.activities.size() == 0
    }

    @Ignore("Need to add follow user API before this will pass")
    void "user is following a single reel"() {
        given:
        def someUserToken = registerNewUserAndGetToken('someone', ALL_SCOPES)
        def videoId = reelTimeClient.uploadVideo(someUserToken)

        def uncategorizedReelId = reelTimeClient.getUncategorizedReelId(someUserToken, 'someone')
        def reelId = reelTimeClient.addReel('some reel', someUserToken)

        when:
        def newsfeed = reelTimeClient.newsfeed(testUserToken)

        then:
        newsfeed.activities.size() == 2

        and:
        newsfeed.activities[0].type == CREATE_REEL_ACTIVITY_TYPE
        newsfeed.activities[0].user.username == 'someone'
                            0
        newsfeed.activities[0].reel.reelId == reelId
        newsfeed.activities[0].reel.name == 'some reel'

        and:
        newsfeed.activities[1].type == ADD_VIDEO_TO_REEL_ACTIVITY_TYPE
        newsfeed.activities[1].user.username == 'someone'

        newsfeed.activities[1].reel.reelId == uncategorizedReelId
        newsfeed.activities[1].reel.name == 'some reel'

        newsfeed.activities[1].video.videoId == videoId
        newsfeed.activities[1].video.title == 'minimum-viable-video'
    }

    void "user is following multiple reels"() {

    }

    void "user is following a single user"() {

    }

    void "user is following multiple users"() {

    }

    void "user is following a mix of users and reels"() {

    }
}
