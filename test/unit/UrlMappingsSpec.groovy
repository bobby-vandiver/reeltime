import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.UrlMappingsUnitTestMixin
import spock.lang.Specification
import in.reeltime.video.transcoder.NotificationController

@TestMixin(UrlMappingsUnitTestMixin)
@Mock([NotificationController])
class UrlMappingsSpec extends Specification {

    // TODO: Figure out why this doesn't work in IntelliJ but does work from the command line
    void "notification endpoint must allow POST"() {
        given:
        webRequest.currentRequest.method = 'POST'

        when:
        assertForwardUrlMapping('/transcoder/notification', controller: 'notification', action: 'jobStatusChange')

        then:
        notThrown(IllegalArgumentException)
    }
}