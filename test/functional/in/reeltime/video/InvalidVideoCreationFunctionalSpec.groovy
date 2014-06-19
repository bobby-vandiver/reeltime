package in.reeltime.video

import grails.plugins.rest.client.RestBuilder
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

    void "title param is missing"() {
        given:
        def token = getAccessTokenWithScope('upload')
        def rest = new RestBuilder()

        when:
        def response = rest.post(BASE_URL + '/video') {
            header 'Authorization', "Bearer $token"
            contentType "multipart/form-data"
            video = new File('test/files/small.mp4')
        }

        then:
        response.statusCode.value() == 400
        response.json.errors.size() == 1
        response.json.errors.contains('[title] is required')
    }
}
