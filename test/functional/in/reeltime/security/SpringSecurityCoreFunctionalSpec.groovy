package in.reeltime.security

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class SpringSecurityCoreFunctionalSpec extends FunctionalSpec {

    @Unroll
    void "cannot access the form login regardless of params"() {
        given:
        def request = new RestRequest(url: springSecurityCheckUrl, customizer: params)

        when:
        def response = post(request)

        then:
        assertAuthError(response, 401, 'unauthorized', 'Full authentication is required to access this resource')

        where:
        _   |   params
        _   |   {}
        _   |   {j_username = 'bob'; j_password = 'pass'}
    }

    void "including a token makes no difference"() {
        given:
        def token = getAccessTokenWithScopeForTestUser('account-read')
        def request = new RestRequest(url: springSecurityCheckUrl, token: token)

        when:
        def response = post(request)

        then:
        response.status == 403
        response.json.error == 'access_denied'
        response.json.error_description == 'Access is denied'
    }
}
