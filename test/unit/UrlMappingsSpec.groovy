import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.UrlMappingsUnitTestMixin
import spock.lang.Specification
import in.reeltime.notification.NotificationController
import in.reeltime.video.VideoCreationController
import in.reeltime.video.VideoRemovalController
import in.reeltime.video.VideoController
import in.reeltime.playlist.PlaylistController
import in.reeltime.playlist.SegmentController
import in.reeltime.account.AccountController
import in.reeltime.account.ResetPasswordController
import in.reeltime.status.ApplicationStatusController
import in.reeltime.reel.ReelController
import in.reeltime.reel.AudienceController
import in.reeltime.activity.NewsfeedController
import in.reeltime.user.UserController
import in.reeltime.user.UserFollowingController
import spock.lang.Unroll

@TestMixin(UrlMappingsUnitTestMixin)
@Mock([VideoCreationController, VideoRemovalController, VideoController,
        PlaylistController, SegmentController, ReelController, AudienceController,
        AccountController, ResetPasswordController, NewsfeedController,
        UserController, UserFollowingController,
        NotificationController, ApplicationStatusController])
class UrlMappingsSpec extends Specification {

    @Unroll
    void "test notification [#action] endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping(url, controller: 'notification', action: action)

        where:
        action          |   url
        'completed'     |   '/transcoder/notification/completed'
        'progressing'   |   '/transcoder/notification/progressing'
        'warning'       |   '/transcoder/notification/warning'
        'error'         |   '/transcoder/notification/error'
    }

    void "test video endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping('/video', controller: 'videoCreation', action: 'upload')
    }

    void "test video status endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/video/1234/status', controller: 'videoCreation', action: 'status')
    }

    void "test video removal endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/video/1234', controller: 'videoRemoval', action: 'remove') {
            videoId = '1234'
        }
    }

    void "test variant playlist streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/playlist/1234', controller: 'playlist', action: 'getVariantPlaylist') {
            videoId = '1234'
        }
    }

    void "test media playlist streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/playlist/12434/949', controller: 'playlist', action: 'getMediaPlaylist') {
            videoId = '12434'
            playlistId = '949'
        }
    }

    void "test media segment streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/playlist/124344/5949/8891', controller: 'segment', action: 'getSegment') {
            videoId = '124344'
            playlistId = '5949'
            segmentId = '8891'
        }
    }

    void "test list reels endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        assertForwardUrlMapping('/user/bob/reels', controller: 'reel', action: 'listUserReels') {
            username = 'bob'
        }
    }

    void "test reel endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping('/reel', controller: 'reel', action: 'addReel')
    }

    void "test list videos in reel endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/reel/1234', controller: 'reel', action: 'listVideos') {
            reelId = '1234'
        }
    }

    void "test add video to reel endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping('/reel/5678', controller: 'reel', action: 'addVideo') {
            reelId = '5678'
        }
    }

    void "test delete reel endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/reel/8675309', controller: 'reel', action: 'deleteReel') {
            reelId = '8675309'
        }
    }

    void "test remove video from reel endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/reel/1234/5678', controller: 'reel', action: 'removeVideo') {
            reelId = '1234'
            videoId = '5678'
        }
    }

    void "test list audience members endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/reel/1234/audience', controller: 'audience', action: 'listMembers') {
            reelId = '1234'
        }
    }

    void "test add audience member endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping('/reel/1234/audience', controller: 'audience', action: 'addMember') {
            reelId = '1234'
        }
    }

    void "test remove audience member endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/reel/1234/audience', controller: 'audience', action: 'removeMember') {
            reelId = '1234'
        }
    }

    void "test registration endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping('/account/register', controller: 'account', action: 'register')
    }

    void "test register new client endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping('/account/client', controller: 'account', action: 'registerClient')
    }

    void "test confirmation endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping('/account/confirm', controller: 'account', action: 'confirm')
    }

    void "test remove account endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/account', controller: 'account', action: 'removeAccount')
    }

    void "test application available mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/available', controller: 'applicationStatus', action: 'available')
    }

    void "test newsfeed mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/newsfeed', controller: 'newsfeed', action: 'listRecentActivity')
    }

    @Unroll
    void "test follow user mapping for http method [#httpMethod] to action [#actionName]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping('/user/bob/follow', controller: 'userFollowing', action: actionName) {
            username = 'bob'
        }

        where:
        httpMethod  |   actionName
        'POST'      |   'followUser'
        'DELETE'    |   'unfollowUser'
    }

    @Unroll
    void "test [#resource] user mapping to action [#actionName]"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping("/user/bob/$resource", controller: 'userFollowing', action: actionName) {
            username = 'bob'
        }

        where:
        resource    |   actionName
        'followers' |   'listFollowers'
        'followees' |   'listFollowees'
    }

    // TODO: Consolidate the above simple tests into this one
    @Unroll
    void "http method [#httpMethod] on [#url] url maps to controller [#controller] action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping(url, controller: controller, action: action)

        where:
        url                         |   httpMethod  |   controller      |   action
        '/users'                    |   'GET'       |   'user'          |   'listUsers'
        '/videos'                   |   'GET'       |   'video'         |   'listVideos'
        '/reels'                    |   'GET'       |   'reel'          |   'listReels'
        '/account/password/email'   |   'POST'      |   'resetPassword' |   'sendEmail'
        '/account/password/reset'   |   'POST'      |   'resetPassword' |   'resetPassword'
    }
}