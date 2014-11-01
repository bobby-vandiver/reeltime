package in.reeltime.account

import groovyx.net.http.HttpResponseException
import helper.oauth2.AccessTokenRequester
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class AccountManagementFunctionalSpec extends FunctionalSpec {

    String token

    String clientId
    String clientSecret

    private static final USERNAME = 'account'

    void setup() {
        token = registerNewUserAndGetToken(USERNAME, ['account-write', 'users-read'])

        def clientCredentials = getClientCredentialsForRegisteredUser(USERNAME)
        clientId = clientCredentials.clientId
        clientSecret = clientCredentials.clientSecret
    }

    @Unroll
    void "invalid http methods for url method [#urlMethod]"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory."${urlMethod}", ['get', 'put', 'delete'])

        where:
        _   |   urlMethod
        _   |   'changeDisplayNameUrl'
        _   |   'changePasswordUrl'
    }

    @Unroll
    void "invalid new display name [#displayName]"() {
        given:
        def request = requestFactory.changeDisplayName(token, displayName)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, message)

        where:
        displayName                     |   message
        null                            |   '[new_display_name] is required'
        ''                              |   '[new_display_name] is required'
        'z'                             |   '[new_display_name] must be 2-20 alphanumeric or space characters long'
        '\'1\' OR \'1\' = \'1\''        |   '[new_display_name] must be 2-20 alphanumeric or space characters long'
        '!zfa2312'                      |   '[new_display_name] must be 2-20 alphanumeric or space characters long'
    }

    @Unroll
    void "invalid new password [#password]"() {
        given:
        def request = requestFactory.changePassword(token, password)

        when:
        def response = post(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, message)

        where:
        password        |   message
        null            |   '[new_password] is required'
        ''              |   '[new_password] is required'
        'a'             |   '[new_password] must be at least 6 characters long'
        'abc24'         |   '[new_password] must be at least 6 characters long'
    }

    void "attempt to revoke client access for unknown client"() {
        given:
        def request = requestFactory.revokeClient(token, clientId + 'a')

        when:
        def response = delete(request)

        then:
        responseChecker.assertStatusCode(response, 403)
    }

    void "successfully change password"() {
        given:
        def oldPassword = TEST_PASSWORD
        def newPassword = TEST_PASSWORD + 'a'

        when:
        reelTimeClient.changePassword(token, newPassword)
        updateUserPassword(USERNAME, newPassword)

        and:
        def postChangeTokenRequest = createAccessTokenRequest(USERNAME, clientId, clientSecret, ALL_SCOPES)
        postChangeTokenRequest.password = newPassword

        then:
        getAccessTokenWithScope(postChangeTokenRequest)

        when:
        def badCredentialsTokenRequest = createAccessTokenRequest(USERNAME, clientId, clientSecret, ALL_SCOPES)
        badCredentialsTokenRequest.password = oldPassword

        def response = AccessTokenRequester.requestAccessToken(badCredentialsTokenRequest.params)

        then:
        response.status == 400
        response.data.error == 'invalid_grant'
        response.data.error_description == 'Bad credentials'
    }

    void "successfully change display name"() {
        given:
        def currentDisplayName = reelTimeClient.userProfile(token, USERNAME).json.display_name
        def newDisplayName = currentDisplayName + 'a'

        when:
        reelTimeClient.changeDisplayName(token, newDisplayName)

        then:
        def changedDisplayName = reelTimeClient.userProfile(token, USERNAME).json.display_name
        changedDisplayName == newDisplayName
    }
}
