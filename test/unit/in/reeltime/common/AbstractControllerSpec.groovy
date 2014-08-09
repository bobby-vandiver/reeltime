package in.reeltime.common

import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import spock.lang.Specification
import static in.reeltime.common.AbstractController.JSON_CONTENT_TYPE

abstract class AbstractControllerSpec extends Specification {

    protected void assertErrorResponse(GrailsMockHttpServletResponse response, int statusCode, Collection<String> messages) {
        assert response.status == statusCode
        assert response.contentType.startsWith(JSON_CONTENT_TYPE)

        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        assert json.size() == 1

        assert json.errors.size() == messages.size()
        messages.each { msg ->
            assert json.errors.contains(msg)
        }
    }
}
