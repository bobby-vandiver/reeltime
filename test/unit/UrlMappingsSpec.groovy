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
    void "httpMethod [#httpMethod] for video url maps to action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping('/api/videos/1234', controller: 'video', action: action) {
            video_id = '1234'
        }

        where:
        httpMethod  |   action
        'GET'       |   'getVideo'
        'DELETE'    |   'removeVideo'
    }

    void "test variant playlist streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/api/playlists/1234', controller: 'playlist', action: 'getVariantPlaylist') {
            video_id = '1234'
        }
    }

    void "test media playlist streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/api/playlists/12434/949', controller: 'playlist', action: 'getMediaPlaylist') {
            video_id = '12434'
            playlist_id = '949'
        }
    }

    void "test media segment streaming endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        expect:
        assertForwardUrlMapping('/api/playlists/124344/5949/8891', controller: 'segment', action: 'getSegment') {
            video_id = '124344'
            playlist_id = '5949'
            segment_id = '8891'
        }
    }

    void "test list reels endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'GET'

        assertForwardUrlMapping('/api/users/bob/reels', controller: 'reel', action: 'listUserReels') {
            username = 'bob'
        }
    }

    @Unroll
    void "httpMethod [#httpMethod] for reel url maps to action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping('/api/reels/8675309', controller: 'reel', action: action) {
            reel_id = '8675309'
        }

        where:
        httpMethod  |   action
        'GET'       |   'getReel'
        'DELETE'    |   'deleteReel'
    }

    @Unroll
    void "httpMethod [#httpMethod] for reel video url maps to action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping('/api/reels/1234/videos', controller: 'reel', action: action) {
            reel_id = '1234'
        }

        where:
        httpMethod  |   action
        'GET'       |   'listVideos'
        'POST'      |   'addVideo'
    }

    void "test remove video from reel endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/api/reels/1234/videos/5678', controller: 'reel', action: 'removeVideo') {
            reel_id = '1234'
            video_id = '5678'
        }
    }

    @Unroll
    void "httpMethod [#httpMethod] for audience url maps to action [#action]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping('/api/reels/1234/audience', controller: 'audience', action: action) {
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
        assertForwardUrlMapping('/api/account/client', controller: 'clientManagement', action: 'registerClient')
    }

    void "test revoke client endpoint mapping"() {
        given:
        webRequest.currentRequest.method = 'DELETE'

        expect:
        assertForwardUrlMapping('/api/account/client/device1', controller: 'clientManagement', action: 'revokeClient') {
            client_id = 'device1'
        }
    }

    @Unroll
    void "test follow user mapping for http method [#httpMethod] to action [#actionName]"() {
        given:
        webRequest.currentRequest.method = httpMethod

        expect:
        assertForwardUrlMapping('/api/users/bob/follow', controller: 'userFollowing', action: actionName) {
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
        assertForwardUrlMapping("/api/users/bob/$resource", controller: 'userFollowing', action: actionName) {
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
        assertForwardUrlMapping('/api/users/bob', controller: 'user', action: 'getUser') {
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
        url                             |   httpMethod  |   controller              |   action
        '/api/users'                    |   'GET'       |   'user'                  |   'listUsers'
        '/api/videos'                   |   'GET'       |   'video'                 |   'listVideos'
        '/api/videos'                   |   'POST'      |   'video'                 |   'upload'
        '/api/reels'                    |   'GET'       |   'reel'                  |   'listReels'
        '/api/reels'                    |   'POST'      |   'reel'                  |   'addReel'
        '/api/newsfeed'                 |   'GET'       |   'newsfeed'              |   'listRecentActivity'
        '/api/account'                  |   'POST'      |   'account'               |   'registerAccount'
        '/api/account'                  |   'DELETE'    |   'account'               |   'removeAccount'
        '/api/account/confirm'          |   'POST'      |   'accountConfirmation'   |   'confirmAccount'
        '/api/account/confirm/email'    |   'POST'      |   'accountConfirmation'   |   'sendEmail'
        '/api/account/display_name'     |   'POST'      |   'accountManagement'     |   'changeDisplayName'
        '/api/account/password'         |   'POST'      |   'accountManagement'     |   'changePassword'
        '/api/account/password/email'   |   'POST'      |   'resetPassword'         |   'sendEmail'
        '/api/account/password/reset'   |   'POST'      |   'resetPassword'         |   'resetPassword'
        '/aws/available'                |   'GET'       |   'applicationStatus'     |   'available'
        '/aws/transcoder/notification'  |   'POST'      |   'notification'          |   'handleMessage'
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