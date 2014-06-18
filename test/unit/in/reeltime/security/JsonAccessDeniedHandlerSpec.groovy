package in.reeltime.security

import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import spock.lang.Specification

class JsonAccessDeniedHandlerSpec extends Specification {

    JsonAccessDeniedHandler jsonAccessDeniedHandler

    void setup() {
        jsonAccessDeniedHandler = new JsonAccessDeniedHandler()
    }

    void "access denied handler returns 403 and no content"() {
        given:
        def request = new GrailsMockHttpServletRequest()
        def response = new GrailsMockHttpServletResponse()
        def exception = Stub(AccessDeniedException) {
            getMessage() >> "TEST"
        }

        when:
        jsonAccessDeniedHandler.handle(request, response, exception)

        then:
        response.status == 403
        response.contentType == 'application/json'

        and:
        def json = new JsonSlurper().parseText(response.contentAsString)
        json.error == 'TEST'
    }
}
