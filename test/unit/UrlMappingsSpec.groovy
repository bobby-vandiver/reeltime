import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.UrlMappingsUnitTestMixin
import spock.lang.Specification
import in.reeltime.notification.NotificationController
import in.reeltime.video.VideoController
import in.reeltime.playlist.PlaylistController
import in.reeltime.playlist.SegmentController
import in.reeltime.account.AccountController
import in.reeltime.account.AccountConfirmationController
import in.reeltime.account.AccountManagementController
import in.reeltime.account.ClientManagementController
import in.reeltime.account.ResetPasswordController
import in.reeltime.account.DevelopmentOnlyAccountController
import in.reeltime.status.ApplicationStatusController
import in.reeltime.reel.ReelController
import in.reeltime.reel.AudienceController
import in.reeltime.activity.NewsfeedController
import in.reeltime.user.UserController
import in.reeltime.user.UserFollowingController
import spock.lang.Unroll

@TestMixin(UrlMappingsUnitTestMixin)
@Mock([VideoController, PlaylistController, SegmentController, ReelController, AudienceController,
        AccountController, AccountConfirmationController, AccountManagementController,
        ClientManagementController, ResetPasswordController, NewsfeedController,
        UserController, UserFollowingController,
        DevelopmentOnlyAccountController, NotificationController, ApplicationStatusController])
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

    void "test video status endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/videos/1234/status', controller: 'video', action: 'status') {
            video_id = '1234'
        }
    }

    void "test video removal endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/videos/1234', controller: 'video', action: 'remove') {
            video_id = '1234'
        }
    }

    void "test variant playlist streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/playlists/1234', controller: 'playlist', action: 'getVariantPlaylist') {
            video_id = '1234'
        }
    }

    void "test media playlist streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/playlists/12434/949', controller: 'playlist', action: 'getMediaPlaylist') {
            video_id = '12434'
            playlist_id = '949'
        }
    }

    void "test media segment streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/playlists/124344/5949/8891', controller: 'segment', action: 'getSegment') {
            video_id = '124344'
            playlist_id = '5949'
            segment_id = '8891'
        }
    }

    void "test list reels endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        assertForwardUrlMapping('/users/bob/reels', controller: 'reel', action: 'listUserReels') {
            username = 'bob'
        }
    }

    @Unroll
    void "httpMethod [#httpMethod] for reel url maps to action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping('/reels/8675309', controller: 'reel', action: action) {
            reel_id = '8675309'
        }

        where:
        httpMethod  |   action
        'GET'       |   'listVideos'
        'POST'      |   'addVideo'
        'DELETE'    |   'deleteReel'
    }

    void "test remove video from reel endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/reels/1234/5678', controller: 'reel', action: 'removeVideo') {
            reel_id = '1234'
            video_id = '5678'
        }
    }

    @Unroll
    void "httpMethod [#httpMethod] for audience url maps to action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping('/reels/1234/audience', controller: 'audience', action: action) {
            reel_id = '1234'
        }

        where:
        httpMethod  |   action
        'GET'       |   'listMembers'
        'POST'      |   'addMember'
        'DELETE'    |   'removeMember'
    }

    void "test register new client endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'POST'

        expect:
        assertForwardUrlMapping('/account/client', controller: 'clientManagement', action: 'registerClient')
    }

    void "test revoke client endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/account/client/device1', controller: 'clientManagement', action: 'revokeClient') {
            client_id = 'device1'
        }
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
        assertForwardUrlMapping('/users/bob/follow', controller: 'userFollowing', action: actionName) {
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
        assertForwardUrlMapping("/users/bob/$resource", controller: 'userFollowing', action: actionName) {
            username = 'bob'
        }

        where:
        resource    |   actionName
        'followers' |   'listFollowers'
        'followees' |   'listFollowees'
    }

    void "test user mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/users/bob', controller: 'user', action: 'getUser') {
            username = 'bob'
        }
    }

    // TODO: Consolidate the above simple tests into this one
    @Unroll
    void "http method [#httpMethod] on [#url] url maps to controller [#controller] action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping(url, controller: controller, action: action)

        where:
        url                         |   httpMethod  |   controller              |   action
        '/users'                    |   'GET'       |   'user'                  |   'listUsers'
        '/videos'                   |   'GET'       |   'video'                 |   'listVideos'
        '/videos'                   |   'POST'      |   'video'                 |   'upload'
        '/reels'                    |   'GET'       |   'reel'                  |   'listReels'
        '/reels'                    |   'POST'      |   'reel'                  |   'addReel'
        '/account'                  |   'POST'      |   'account'               |   'registerAccount'
        '/account'                  |   'DELETE'    |   'account'               |   'removeAccount'
        '/account/confirm'          |   'POST'      |   'accountConfirmation'   |   'confirmAccount'
        '/account/confirm/email'    |   'POST'      |   'accountConfirmation'   |   'sendEmail'
        '/account/display_name'     |   'POST'      |   'accountManagement'     |   'changeDisplayName'
        '/account/password'         |   'POST'      |   'accountManagement'     |   'changePassword'
        '/account/password/email'   |   'POST'      |   'resetPassword'         |   'sendEmail'
        '/account/password/reset'   |   'POST'      |   'resetPassword'         |   'resetPassword'
    }

    @Unroll
    void "internal development only url [#url] maps to controller [#controller] action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping(url, controller: controller, action: action) {
            username = 'bob'
        }

        where:
        url                         |   httpMethod  |   controller                  |   action
        '/internal/bob/confirm'     |   'POST'      |   'developmentOnlyAccount'    |   'confirmAccountForUser'
        '/internal/bob/password'    |   'POST'      |   'developmentOnlyAccount'    |   'resetPasswordForUser'
    }
}