package in.reeltime.registration

import grails.plugins.rest.client.RestResponse
import helper.oauth2.AccessTokenRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class RegistrationFunctionalSpec extends FunctionalSpec {

    @Override
    protected String getResource() {
        return '/register'
    }

    @Unroll
    void "invalid http method [#method]"() {
        when:
        def response = "$method"() as RestResponse

        then:
        response.status == 405
        response.body == ''

        where:
        _   |   method
        _   |   'get'
        _   |   'put'
        _   |   'delete'
    }

    void "register a new user"() {
        when:
        def response = post() {
            username = 'newUser'
            password = 'newPassword'
            client_name = 'newClient'
        }

        then:
        response.status == 201
        response.json.client_id
        response.json.client_secret

        and:
        def clientId = response.json.client_id
        def clientSecret = response.json.client_secret

        and:
        def tokenRequest = new AccessTokenRequest(
                clientId: clientId,
                clientSecret: clientSecret,
                username: 'newUser',
                password: 'newPassword',
                grantType: 'password',
                scope: ['view']
        )

        when:
        def token = getAccessTokenWithScope(tokenRequest)

        then:
        token != null
    }
}
