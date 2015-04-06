package in.reeltime.reel

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.*
import in.reeltime.video.Video
import spock.lang.Unroll

@TestFor(ReelController)
@Mock([Reel, Video, ReelVideo])
class ReelControllerSpec extends AbstractControllerSpec {

    ReelService reelService
    ReelCreationService reelCreationService
    ReelRemovalService reelRemovalService
    ReelVideoManagementService reelVideoManagementService

    Reel reel
    Long reelId

    void setup() {
        reelService = Mock(ReelService)
        reelCreationService = Mock(ReelCreationService)
        reelRemovalService = Mock(ReelRemovalService)
        reelVideoManagementService = Mock(ReelVideoManagementService)

        controller.reelService = reelService
        controller.reelCreationService = reelCreationService
        controller.reelRemovalService = reelRemovalService
        controller.reelVideoManagementService = reelVideoManagementService

        reel = new Reel(name: 'test').save(validate: false)
        reelId = reel.id

        defineBeans {
            reelAuthorizationService(ReelAuthorizationService)
        }
    }

    void "get reel"() {
        given:
        def reel = new Reel(name: 'test').save(validate: false)
        def reelId = reel.id

        and:
        params.reel_id = reelId

        when:
        controller.getReel()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)

        and:
        json.reel_id == reelId
        json.name == 'test'
        json.audience_size == 0
        json.video_count == 0

        and:
        1 * reelService.loadReel(reelId) >> reel
    }

    void "use page 1 if page param is omitted"() {
        when:
        controller.listReels()

        then:
        1 * reelService.listReels(1) >> []
    }

    void "list reels"() {
        given:
        params.page = 3

        and:
        def reel = new Reel(name: 'foo').save(validate: false)

        when:
        controller.listReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.reels.size() == 1

        and:
        json.reels[0].reel_id == reel.id
        json.reels[0].name == 'foo'

        and:
        1 * reelService.listReels(3) >> [reel]
    }

    void "use page 1 for user reels list if page param is omitted"() {
        given:
        params.username = 'bob'

        when:
        controller.listUserReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        1 * reelService.listReelsByUsername('bob', 1) >> []
    }

    void "specify page for user reels list"() {
        given:
        params.username = 'bob'
        params.page = 42

        when:
        controller.listUserReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        1 * reelService.listReelsByUsername('bob', 42) >> []
    }

    void "empty reels list"() {
        given:
        params.username = 'bob'

        when:
        controller.listUserReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.reels.size() == 0

        and:
        1 * reelService.listReelsByUsername('bob', _) >> []
    }

    void "reels list contains only one reel"() {
        given:
        def reel = new Reel(name: 'foo').save(validate: false)
        assert reel.id > 0

        and:
        params.username = 'bob'

        when:
        controller.listUserReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.reels.size() == 1

        and:
        json.reels[0].reel_id == reel.id
        json.reels[0].name == 'foo'
        json.reels[0].audience_size == 0
        json.reels[0].video_count == 0

        and:
        1 * reelService.listReelsByUsername('bob', _) >> [reel]
    }

    void "reels list contains multiple reels"() {
        given:
        def reel1 = new Reel(name: 'foo').save(validate: false)
        assert reel1.id > 0

        and:
        def reel2 = new Reel(name: 'bar').save(validate: false)
        assert reel2.id > 0

        and:
        params.username = 'bob'

        when:
        controller.listUserReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.reels.size() == 2

        and:
        json.reels[0].reel_id == reel1.id
        json.reels[0].name == 'foo'
        json.reels[0].audience_size == 0
        json.reels[0].video_count == 0

        and:
        json.reels[1].reel_id == reel2.id
        json.reels[1].name == 'bar'
        json.reels[1].audience_size == 0
        json.reels[1].video_count == 0

        and:
        1 * reelService.listReelsByUsername('bob', _) >> [reel1, reel2]
    }

    void "cannot list reels for unknown user"() {
        given:
        def username = 'someone'
        params.username = username

        and:
        def message = 'unknown username'

        when:
        controller.listUserReels()

        then:
        assertErrorMessageResponse(response, 404, message)

        and:
        1 * reelService.listReelsByUsername(username, _) >> { throw new UserNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('user.unknown', request.locale) >> message
    }

    void "successfully add a new reel"() {
        given:
        def reelName = 'test-reel-name'
        def reel = new Reel(name: reelName).save(validate: false)

        and:
        params.name = reelName

        when:
        controller.addReel()

        then:
        assertStatusCodeAndContentType(response, 201)

        and:
        def json = getJsonResponse(response)
        json.reel_id == reel.id
        json.name == reelName
        json.audience_size == 0
        json.video_count == 0

        and:
        1 * reelCreationService.addReel(reelName) >> reel
    }

    void "unable to add reel with invalid name"() {
        given:
        def reelName = 'invalid-reel'
        params.name = reelName

        and:
        def message = 'reel bad name'

        when:
        controller.addReel()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * reelCreationService.addReel(reelName) >> { throw new InvalidReelNameException('TEST') }
        1 * localizedMessageService.getMessage('addReel.name.invalid', request.locale) >> message
    }

    void "successfully delete a reel"() {
        given:
        params.reel_id = reelId

        when:
        controller.deleteReel()

        then:
        response.status == 200
        response.contentLength == 0

        and:
        1 * reelService.loadReel(reelId) >> reel
        1 * reelRemovalService.removeReel(reel)
    }

    void "attempt to delete an unknown reel"() {
        given:
        def reelId = 9431
        params.reel_id = reelId

        when:
        controller.deleteReel()

        then:
        assertErrorMessageResponse(response, 404, TEST_MESSAGE)

        and:
        1 * reelService.loadReel(reelId) >> { throw new ReelNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('reel.unknown', request.locale) >> TEST_MESSAGE
    }

    void "use page 1 for videos in reel list if page param is omitted"() {
        given:
        params.reel_id = 1234

        when:
        controller.listVideos()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        1 * reelVideoManagementService.listVideosInReel(1234, 1) >> []
    }

    void "specify page for videos in reel list"() {
        given:
        params.reel_id = 1234
        params.page = 21

        when:
        controller.listVideos()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        1 * reelVideoManagementService.listVideosInReel(1234, 21) >> []
    }

    void "empty list of videos for reel"() {
        given:
        params.reel_id = 1234

        when:
        controller.listVideos()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.videos.size() == 0

        and:
        1 * reelVideoManagementService.listVideosInReel(1234, _) >> []
    }

    void "video list contains only one video"() {
        given:
        def video = new Video(title: 'one').save(validate: false)
        assert video.id > 0

        and:
        params.reel_id = 1234

        when:
        controller.listVideos()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.videos.size() == 1

        and:
        json.videos[0].size() == 2
        json.videos[0].video_id == video.id
        json.videos[0].title == 'one'

        and:
        1 * reelVideoManagementService.listVideosInReel(1234, _) >> [video]
    }

    void "video list contains multiple videos"() {
        given:
        def video1 = new Video(title: 'first').save(validate: false)
        assert video1.id > 0

        and:
        def video2 = new Video(title: 'second').save(validate: false)
        assert video2.id > 0

        and:
        params.reel_id = 1234

        when:
        controller.listVideos()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.videos.size() == 2

        and:
        json.videos[0].size() == 2
        json.videos[0].video_id == video1.id
        json.videos[0].title == 'first'

        and:
        json.videos[1].size() == 2
        json.videos[1].video_id == video2.id
        json.videos[1].title == 'second'

        and:
        1 * reelVideoManagementService.listVideosInReel(1234, _) >> [video1, video2]
    }

    void "attempt to list videos for an unknown reel"() {
        given:
        def message = 'unknown reel'

        and:
        def reelId = 9431
        params.reel_id = reelId

        when:
        controller.listVideos()

        then:
        assertErrorMessageResponse(response, 404, message)

        and:
        1 * reelVideoManagementService.listVideosInReel(reelId, _) >> { throw new ReelNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('reel.unknown', request.locale) >> message
    }

    void "attempt to add video to unknown reel"() {
        given:
        def reelId = 9431
        def videoId = 81813

        and:
        params.reel_id = reelId
        params.video_id = videoId

        when:
        controller.addVideo()

        then:
        assertErrorMessageResponse(response, 404, TEST_MESSAGE)

        and:
        1 * reelVideoManagementService.addVideo(reelId, videoId) >> { throw new ReelNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('reel.unknown', request.locale) >> TEST_MESSAGE
    }

    void "attempt to add video when not authorized"() {
        given:
        def reelId = 9431
        def videoId = 81813

        and:
        params.reel_id = reelId
        params.video_id = videoId

        when:
        controller.addVideo()

        then:
        assertStatusCode(response, 403)

        and:
        1 * reelVideoManagementService.addVideo(reelId, videoId) >> { throw new AuthorizationException('TEST') }
    }

    void "successfully add a video to a reel"() {
        given:
        def reelId = 9431
        def videoId = 81813

        and:
        params.reel_id = reelId
        params.video_id = videoId

        when:
        controller.addVideo()

        then:
        response.status == 201
        response.contentLength == 0

        and:
        1 * reelVideoManagementService.addVideo(reelId, videoId)
    }

    @Unroll
    void "attempt to remove video from reel throws [#exceptionClass]"() {
        given:
        def message = 'TEST'

        and:
        def reelId = 9431
        def videoId = 81813

        and:
        params.reel_id = reelId
        params.video_id = videoId

        when:
        controller.removeVideo()

        then:
        assertErrorMessageResponse(response, statusCode, message)

        and:
        1 * reelVideoManagementService.removeVideo(reelId, videoId) >> { throw exceptionClass.newInstance('TEST') }
        1 * localizedMessageService.getMessage(messageCode, request.locale) >> message

        where:
        exceptionClass          |   statusCode  |   messageCode
        ReelNotFoundException   |   404         |   'reel.unknown'
        VideoNotFoundException  |   404         |   'video.unknown'
    }

    void "attempt to remove video when not authorized"() {
        given:
        def reelId = 9431
        def videoId = 81813

        and:
        params.reel_id = reelId
        params.video_id = videoId

        when:
        controller.removeVideo()

        then:
        assertStatusCode(response, 403)

        and:
        1 * reelVideoManagementService.removeVideo(reelId, videoId) >> { throw new AuthorizationException('TEST') }
    }

    void "successfully remove a video from a reel"() {
        given:
        def reelId = 9431
        def videoId = 81813

        and:
        params.reel_id = reelId
        params.video_id = videoId

        when:
        controller.removeVideo()

        then:
        response.status == 200
        response.contentLength == 0

        and:
        1 * reelVideoManagementService.removeVideo(reelId, videoId)
    }
}
