package in.reeltime.common

import groovy.json.JsonSlurper
import in.reeltime.message.LocalizedMessageService
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import in.reeltime.security.AuthenticationService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
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

    protected void stubAuthenticationService(boolean authenticated) {
        defineBeans {
            authenticationService(AuthenticationService)
        }
        def authenticationService = grailsApplication.mainContext.getBean('authenticationService')

        authenticationService.authenticationManager = Stub(AuthenticationManager) {
            authenticate(_) >> {
                if(!authenticated) {
                    throw new BadCredentialsException('TEST')
                }
            }
        }

        // Workaround for GRAILS-10538 per comment by Aaron Long:
        // Source: https://jira.grails.org/browse/GRAILS-10538
        authenticationService.transactionManager = Mock(PlatformTransactionManager) {
            getTransaction(_) >> Mock(TransactionStatus)
        }
    }
}
