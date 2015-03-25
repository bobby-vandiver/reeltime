package in.reeltime.thumbnail

import grails.test.spock.IntegrationSpec
import spock.lang.Unroll
import test.helper.FileLoader

class ThumbnailValidationServiceIntegrationSpec extends IntegrationSpec {

    def thumbnailValidationService

    void "null stream is invalid"() {
        when:
        def result = thumbnailValidationService.validateThumbnailStream(null)

        then:
        !result.validFormat
    }

    @Unroll
    void "thumbnail [#filename] format is valid [#valid]"() {
        given:
        def file = FileLoader.imageFile(filename)
        def stream = new FileInputStream(file)

        when:
        def result = thumbnailValidationService.validateThumbnailStream(stream)

        then:
        result.validFormat == valid

        where:
        filename                |   valid
        'batman.png'            |   true
        'boogie2988-rage.png'   |   true
        'boxxy.jpg'             |   false
        'deathscythe.jpg'       |   false
        'maddox.jpg'            |   false
        'empty'                 |   false
        'random'                |   false
    }
}
