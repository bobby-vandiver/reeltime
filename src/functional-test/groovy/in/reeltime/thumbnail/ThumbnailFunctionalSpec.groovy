package in.reeltime.thumbnail

import grails.plugins.rest.client.RestResponse
import in.reeltime.test.spec.FunctionalSpec
import spock.lang.Unroll

import java.awt.image.BufferedImage

class ThumbnailFunctionalSpec extends FunctionalSpec {

    String token
    Long videoId

    void setup() {
        token = getAccessTokenWithScopesForTestUser(['videos-read', 'videos-write'])
        videoId = reelTimeClient.uploadVideoToUncategorizedReel(token, 'thumbnailTest')
    }

    void "invalid http methods"() {
        given:
        def url = urlFactory.getThumbnailUrl(videoId)

        expect:
        responseChecker.assertInvalidHttpMethods(url, ['put', 'post', 'delete'])
    }

    @Unroll
    void "invalid resolution [#resolution]"() {
        given:
        def request = requestFactory.getThumbnail(token, videoId, resolution)

        when:
        def response = get(request)

        then:
        responseChecker.assertSingleErrorMessageResponse(response, 400, message)

        where:
        resolution  |   message
        null        |   '[resolution] is required'
        '1'         |   '[resolution] must be small, medium or large'
    }

    @Unroll
    void "retrieve [#resolution] thumbnail"() {
        given:
        def request = requestFactory.getThumbnail(token, videoId, resolution)
        request.customizer = { accept BufferedImage }

        when:
        def response = get(request)

        then:
        assertThumbnailInResponse(response, height, width)

        where:
        resolution  |   height  |   width
        'small'     |   75      |   75
        'medium'    |   150     |   150
        'large'     |   225     |   225
    }

    private void assertThumbnailInResponse(RestResponse response, int height, int width) {
        responseChecker.assertStatusCode(response, 200)
        responseChecker.assertContentType(response, 'image/png')

        def thumbnail = response.body as BufferedImage

        assert thumbnail.height == height
        assert thumbnail.width == width
    }
}
