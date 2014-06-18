package in.reeltime.video

import groovyx.net.http.HttpResponseDecorator
import in.reeltime.FunctionalSpec
import spock.lang.Ignore

class VideoCreationFunctionalSpec extends FunctionalSpec {

    @Ignore("Need to configure Spring Security plugin for unauthorized response")
    void "unauthorized video upload: no token present"() {
        when:
        def response = restClient.post(path: 'video') as HttpResponseDecorator

        then:
        response.status == 401
    }

    void "unauthorized video upload: token does not have upload scope"() {
        given:
        def token = getAccessTokenWithScope('view')
        def headers = [Authorization: "Bearer $token"]

        when:
        def response = restClient.post(path: 'video', headers: headers) as HttpResponseDecorator

        then:
        response.status == 403
        response.data.error == 'Insufficient scope for this resource'
    }

    void "unauthorized video upload: invalid token"() {
        given:
        def token = 'invalid-token'
        def headers = [Authorization: "Bearer $token"]

        when:
        def response = restClient.post(path: 'video', headers: headers) as HttpResponseDecorator

        then:
        response.status == 401
        response.data.error == 'invalid_token'
    }
}
