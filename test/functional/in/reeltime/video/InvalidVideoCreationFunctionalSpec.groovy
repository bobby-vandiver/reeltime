package in.reeltime.video

import groovyx.net.http.HttpResponseDecorator
import in.reeltime.FunctionalSpec

class InvalidVideoCreationFunctionalSpec extends FunctionalSpec {

    void "no token present"() {
        when:
        def response = restClient.post(path: 'video') as HttpResponseDecorator

        then:
        response.status == 401
        response.data.error == 'unauthorized'
        response.data.error_description == 'Full authentication is required to access this resource'
    }

    void "token does not have upload scope"() {
        given:
        def token = getAccessTokenWithScope('view')
        def headers = [Authorization: "Bearer $token"]

        when:
        def response = restClient.post(path: 'video', headers: headers) as HttpResponseDecorator

        then:
        response.status == 403
        response.data.scope == 'upload'
        response.data.error == 'insufficient_scope'
        response.data.error_description == 'Insufficient scope for this resource'
    }

    void "invalid token"() {
        given:
        def token = 'bad-mojo'
        def headers = [Authorization: "Bearer $token"]

        when:
        def response = restClient.post(path: 'video', headers: headers) as HttpResponseDecorator

        then:
        response.status == 401
        response.data.error == 'invalid_token'
        response.data.error_description == 'Invalid access token: bad-mojo'
    }
}
