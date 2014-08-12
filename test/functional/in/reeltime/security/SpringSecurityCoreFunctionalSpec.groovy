package in.reeltime.security

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class SpringSecurityCoreFunctionalSpec extends FunctionalSpec {

    private final String SPRING_SECURITY_CHECK_URL = getUrlForResource('j_spring_security_check')

    @Unroll
    void "cannot access the form login regardless of params"() {
        given:
        def request = new RestRequest(url: SPRING_SECURITY_CHECK_URL, customizer: params)

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
        def token = getAccessTokenWithScope('account-read')
        def request = new RestRequest(url: SPRING_SECURITY_CHECK_URL, token: token)

        when:
        def response = post(request)

        then:
        response.status == 403
        response.json.error == 'access_denied'
        response.json.error_description == 'Access is denied'
    }
}
