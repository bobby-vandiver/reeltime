package in.reeltime.common

import grails.test.spock.IntegrationSpec
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse

import static in.reeltime.common.ContentTypes.getAPPLICATION_JSON

// Duplicate of AbstractControllerSpec since Grails doesn't provide
// a way to share helper classes between unit and integration tests
class AbstractControllerIntegrationSpec extends IntegrationSpec {

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

    protected void assertResponseHasErrors(GrailsMockHttpServletResponse response, int statusCode) {
        assertStatusCodeAndContentType(response, statusCode)

        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        assert json.size() == 1
        assert json.errors.size() == 1
    }
}
