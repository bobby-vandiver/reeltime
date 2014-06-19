package in.reeltime.security

import groovyx.net.http.HttpResponseDecorator
import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class SpringSecurityCoreFunctionalSpec extends FunctionalSpec {

    @Unroll
    void "cannot access the form login regardless of params [#params]"() {
        when:
        def response = restClient.post(path: 'j_spring_security_check', query: params) as HttpResponseDecorator

        then:
        response.status == 401
        response.data.error == 'unauthorized'
        response.data.error_description == 'Full authentication is required to access this resource'

        where:
        _   |   params
        _   |   [:]
        _   |   [j_username: 'bob', j_password: 'pass']
    }

    void "including a token makes no difference"() {
        given:
        def token = getAccessTokenWithScope('view upload')
        def headers = [Authorization: "Bearer $token"]

        when:
        def response = restClient.post(path: 'j_spring_security_check', headers: headers) as HttpResponseDecorator

        then:
        response.status == 403
        response.data.error == 'access_denied'
        response.data.error_description == 'Access is denied'
    }
}
