import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.UrlMappingsUnitTestMixin
import spock.lang.Specification
import in.reeltime.notification.NotificationController
import in.reeltime.video.VideoCreationController
import in.reeltime.playlist.PlaylistController
import in.reeltime.playlist.SegmentController
import in.reeltime.registration.RegistrationController

@TestMixin(UrlMappingsUnitTestMixin)
@Mock([NotificationController, VideoCreationController, PlaylistController, SegmentController, RegistrationController])
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

    void "test registration endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/register', controller: 'registration', action: 'register')
    }
}