package in.reeltime.activity

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.message.LocalizedMessageService
import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video
import spock.lang.Unroll

@TestFor(NewsfeedController)
@Mock([User, Reel, Video])
class NewsfeedControllerSpec extends AbstractControllerSpec {

    NewsfeedService newsfeedService

    void setup() {
        newsfeedService = Mock(NewsfeedService)
        controller.newsfeedService = newsfeedService
    }

    void "page should be 1 if not included in request"() {
        when:
        controller.listRecentActivity()

        then:
        1 * newsfeedService.listRecentActivity(1)
    }

    @Unroll
    void "request page [#pageNumber]"() {
        given:
        params.page = pageNumber

        when:
        controller.listRecentActivity()

        then:
        1 * newsfeedService.listRecentActivity(pageNumber)

        where:
        _   |   pageNumber
        _   |   1
        _   |   2
        _   |   5
        _   |   100
    }

    void "return activity list"() {
        given:
        def user = new User(username: 'someone')
        def reel = new Reel(name: 'foo').save(validate: false)
        def video = new Video(id: 5678, title: 'bar').save(validate: false)

        def reelId = reel.id
        def videoId = video.id

        def activities = [
                new UserReelVideoActivity(user: user, reel: reel, video: video, type: ActivityType.AddVideoToReel),
                new UserReelActivity(user: user, reel: reel, type: ActivityType.CreateReel)
        ]

        when:
        controller.listRecentActivity()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response) as Map
        json.activities.size() == 2

        and:
        json.activities[0].size() == 4

        json.activities[0].type == 'add-video-to-reel'
        json.activities[0].user.username == 'someone'

        json.activities[0].reel.reelId == reelId
        json.activities[0].reel.name == 'foo'

        json.activities[0].video.videoId == videoId
        json.activities[0].video.title == 'bar'

        and:
        json.activities[1].size() == 3

        json.activities[1].type == 'create-reel'
        json.activities[1].user.username == 'someone'

        json.activities[1].reel.reelId == reelId
        json.activities[1].reel.name == 'foo'

        1 * newsfeedService.listRecentActivity(1) >> activities
    }
}
