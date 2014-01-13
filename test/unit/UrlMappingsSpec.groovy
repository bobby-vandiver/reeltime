import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.UrlMappingsUnitTestMixin
import spock.lang.Specification
import in.reeltime.notification.NotificationController
import in.reeltime.video.VideoController

@TestMixin(UrlMappingsUnitTestMixin)
@Mock([NotificationController, VideoController])
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
        assertForwardUrlMapping('/video', controller: 'video', action: 'upload')
    }
}