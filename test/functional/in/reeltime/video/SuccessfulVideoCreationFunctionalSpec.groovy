package in.reeltime.video

import in.reeltime.FunctionalSpec

class SuccessfulVideoCreationFunctionalSpec extends FunctionalSpec {

    String uploadToken

    @Override
    protected String getResource() {
        return 'video'
    }

    void setup() {
        uploadToken = getAccessTokenWithScope('upload')
    }

    void "minimum required params"() {
        when:
        def response = postFormData(uploadToken) {
            title = 'minimum-viable-video'
            video = new File('test/files/small.mp4')
        }

        then:
        response.status == 202
        response.json.size() == 1
        response.json.videoId > 0
    }
}
