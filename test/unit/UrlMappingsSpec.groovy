import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.UrlMappingsUnitTestMixin
import spock.lang.Specification
import in.reeltime.video.transcoder.NotificationController

@TestMixin(UrlMappingsUnitTestMixin)
@Mock([NotificationController])
class UrlMappingsSpec extends Specification {

    void "test notification endpoint mapping"() {
        expect:
        assertForwardUrlMapping('/transcoder/notification/completed', controller: 'notification', action: 'completed')
        assertForwardUrlMapping('/transcoder/notification/progressing', controller: 'notification', action: 'progressing')
        assertForwardUrlMapping('/transcoder/notification/warning', controller: 'notification', action: 'warning')
        assertForwardUrlMapping('/transcoder/notification/error', controller: 'notification', action: 'error')
    }
}