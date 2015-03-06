package in.reeltime.common

import groovy.json.JsonSlurper
import in.reeltime.message.LocalizedMessageService
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import spock.lang.Specification

import static in.reeltime.common.ContentTypes.APPLICATION_JSON

abstract class AbstractControllerSpec extends Specification {

    protected LocalizedMessageService localizedMessageService

    protected final String TEST_MESSAGE = 'this is a test'

    void setup() {
        localizedMessageService = Mock(LocalizedMessageService)
        controller.localizedMessageService = localizedMessageService

        // TODO: Re-enable when GRAILS-11116 is resolved
        // new CustomMarshallerRegistrar().registerMarshallers()
    }

    protected Object getJsonResponse(GrailsMockHttpServletResponse response) {
        new JsonSlurper().parseText(response.contentAsString)
    }

    protected void assertStatusCodeAndContentType(GrailsMockHttpServletResponse response, int statusCode) {
        assert response.status == statusCode
        assert response.contentType.startsWith(APPLICATION_JSON)
    }

    protected void assertStatusCode(GrailsMockHttpServletResponse response, int statusCode) {
        assert response.status == statusCode
    }

    protected void assertErrorMessageResponse(GrailsMockHttpServletResponse response, int statusCode, String message) {
        assertStatusCodeAndContentType(response, statusCode)

        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        assert json.size() == 1
        assert json.errors == [message]
    }
}
