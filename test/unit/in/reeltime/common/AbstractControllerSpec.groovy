package in.reeltime.common

import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import spock.lang.Specification
import static in.reeltime.common.AbstractController.JSON_CONTENT_TYPE

abstract class AbstractControllerSpec extends Specification {

    protected void assertStatusCodeAndContentType(GrailsMockHttpServletResponse response, int statusCode) {
        assert response.status == statusCode
        assert response.contentType.startsWith(JSON_CONTENT_TYPE)
    }

    protected void assertErrorMessageResponse(GrailsMockHttpServletResponse response, int statusCode, String message) {
        assertStatusCodeAndContentType(response, statusCode)

        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        assert json.size() == 1
        assert json.errors == [message]
    }
}
