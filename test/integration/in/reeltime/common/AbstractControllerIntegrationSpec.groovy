package in.reeltime.common

import grails.test.spock.IntegrationSpec
import groovy.json.JsonSlurper
import in.reeltime.user.User
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import in.reeltime.account.AccountCode
import in.reeltime.account.AccountCodeType

import static in.reeltime.common.ContentTypes.APPLICATION_JSON

// Duplicate of AbstractControllerSpec since Grails doesn't provide
// a way to share helper classes between unit and integration tests
class AbstractControllerIntegrationSpec extends IntegrationSpec {

    protected Map getJsonResponse(GrailsMockHttpServletResponse response) {
        new JsonSlurper().parseText(response.contentAsString)
    }

    protected void assertStatusCodeAndContentType(GrailsMockHttpServletResponse response, int statusCode) {
        assert response.status == statusCode
        assert response.contentType.startsWith(APPLICATION_JSON)
    }

    protected void assertStatusCodeOnlyResponse(GrailsMockHttpServletResponse response, int statusCode) {
        assert response.status == statusCode
        assert response.contentAsByteArray.size() == 0
    }

    protected void assertResponseHasErrors(GrailsMockHttpServletResponse response, int statusCode) {
        assertStatusCodeAndContentType(response, statusCode)

        def json = new JsonSlurper().parseText(response.contentAsString) as Map
        assert json.size() == 1
        assert json.errors.size() >= 1
    }

    protected void assertResetCodeHasBeenRemoved(User user) {
        assertAccountCodeExistenceForUser(user, AccountCodeType.ResetPassword, false)
    }

    protected void assertResetCodeIsAvailable(User user) {
        assertAccountCodeExistenceForUser(user, AccountCodeType.ResetPassword, true)
    }

    protected void assertConfirmationCodeHasBeenRemoved(User user) {
        assertAccountCodeExistenceForUser(user, AccountCodeType.AccountConfirmation, false)
    }

    protected void assertConfirmationCodeIsAvailable(User user) {
        assertAccountCodeExistenceForUser(user, AccountCodeType.AccountConfirmation, true)
    }

    private assertAccountCodeExistenceForUser(User user, AccountCodeType type, boolean shouldExist) {
        def accountCodeExists = AccountCode.findByUserAndType(user, type) != null
        assert accountCodeExists == shouldExist
    }
}
