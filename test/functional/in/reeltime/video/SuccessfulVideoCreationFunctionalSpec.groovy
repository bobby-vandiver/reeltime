package in.reeltime.video

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec

class SuccessfulVideoCreationFunctionalSpec extends FunctionalSpec {

    String uploadToken

    void setup() {
        uploadToken = getAccessTokenWithScope('upload')
    }

    void "minimum required params"() {
        given:
        def request = createRequest(uploadToken) {
            title = 'minimum-viable-video'
            video = new File('test/files/small.mp4')
        }

        when:
        def response = post(request)

        then:
        response.status == 202
        response.json.size() == 1
        response.json.videoId > 0
    }

    private static getUploadUrl() {
        getUrlForResource('video')
    }

    private static RestRequest createRequest(String token = null, Closure params = null) {
        new RestRequest(url: uploadUrl, token: token, isMultiPart: params != null, customizer: params)
    }
}
