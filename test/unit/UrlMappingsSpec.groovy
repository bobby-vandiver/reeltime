import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.UrlMappingsUnitTestMixin
import spock.lang.Specification
import in.reeltime.notification.NotificationController
import in.reeltime.video.VideoCreationController
import in.reeltime.playlist.PlaylistController
import in.reeltime.playlist.SegmentController
import in.reeltime.account.AccountController
import in.reeltime.status.ApplicationStatusController
import in.reeltime.reel.ReelController

@TestMixin(UrlMappingsUnitTestMixin)
@Mock([NotificationController, VideoCreationController, PlaylistController,
        SegmentController, ReelController, AccountController, ApplicationStatusController])
class UrlMappingsSpec extends Specification {

    void "test notification endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/transcoder/notification/completed', controller: 'notification', action: 'completed')
        assertForwardUrlMapping('/transcoder/notification/progressing', controller: 'notification', action: 'progressing')
        assertForwardUrlMapping('/transcoder/notification/warning', controller: 'notification', action: 'warning')
        assertForwardUrlMapping('/transcoder/notification/error', controller: 'notification', action: 'error')
    }

    void "test video endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/video', controller: 'videoCreation', action: 'upload')
    }

    void "test video status endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/video/1234/status', controller: 'videoCreation', action: 'status')
    }

    void "test variant playlist streaming endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/video/1234', controller: 'playlist', action: 'getVariantPlaylist') {
            videoId = '1234'
        }
    }

    void "test media playlist streaming endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/video/12434/949', controller: 'playlist', action: 'getMediaPlaylist') {
            videoId = '12434'
            playlistId = '949'
        }
    }

    void "test media segment streaming endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/video/124344/5949/8891', controller: 'segment', action: 'getSegment') {
            videoId = '124344'
            playlistId = '5949'
            segmentId = '8891'
        }
    }

    void "test list reels endpoint mapping"() {
        assertForwardUrlMapping('/user/bob/reels', controller: 'reel', action: 'listReels') {
            username = 'bob'
        }
    }

    void "test reel endpoint mapping"() {
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
        expect:
        assertForwardUrlMapping('/reel/1234/5678', controller: 'reel', action: 'removeVideo') {
            reelId = '1234'
            videoId = '5678'
        }
    }

    void "test registration endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/account/register', controller: 'account', action: 'register')
    }

    void "test confirmation endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/account/confirm', controller: 'account', action: 'confirm')
    }

    void "test application available mapping"() {
        expect:
        assertForwardUrlMapping('/available', controller: 'applicationStatus', action: 'available')
    }
}