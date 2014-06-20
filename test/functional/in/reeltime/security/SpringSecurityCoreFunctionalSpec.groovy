package in.reeltime.security

import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class SpringSecurityCoreFunctionalSpec extends FunctionalSpec {

    @Override
    protected String getResource() {
        return 'j_spring_security_check'
    }

    @Unroll
    void "cannot access the form login regardless of params"() {
        when:
        def response = restClient.post(endpoint, params)

        then:
        assertAuthError(response, 401, 'unauthorized', 'Full authentication is required to access this resource')

        where:
        _   |   params
        _   |   {}
        _   |   {j_username = 'bob'; j_password = 'pass'}
    }

    void "including a token makes no difference"() {
        given:
        def token = getAccessTokenWithScope('view upload')

        when:
        def response = post(token)

        then:
        response.status == 403
        response.json.error == 'access_denied'
        response.json.error_description == 'Access is denied'
    }
}
