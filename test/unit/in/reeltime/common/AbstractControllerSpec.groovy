package in.reeltime.common

import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import spock.lang.Specification
import static in.reeltime.common.ContentTypes.APPLICATION_JSON

abstract class AbstractControllerSpec extends Specification {

    protected final String TEST_MESSAGE = 'this is a test'

    protected Object getJsonResponse(GrailsMockHttpServletResponse response) {
        new JsonSlurper().parseText(response.contentAsString)
    }

    protected void assertStatusCodeAndContentType(GrailsMockHttpServletResponse response, int statusCode) {
        assert response.status == statusCode
        assert response.contentType.startsWith(APPLICATION_JSON)
    }

    protected void assertStatusCodeOnlyResponse(GrailsMockHttpServletResponse response, int statusCode) {
        assert response.status == statusCode
        assert response.contentLength == 0
    }

    protected void assertErrorMessageResponse(GrailsMockHttpServletResponse response, int statusCode, String message) {
        assertStatusCodeAndContentType(response, statusCode)

        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        assert json.size() == 1
        assert json.errors == [message]
    }
}
