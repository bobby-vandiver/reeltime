package in.reeltime.thumbnail

import grails.test.spock.IntegrationSpec
import spock.lang.Unroll
import test.helper.FileLoader

class ThumbnailValidationServiceIntegrationSpec extends IntegrationSpec {

    def thumbnailValidationService

    @Unroll
    void "thumbnail [#filename] is valid [#valid]"() {
        given:
        def file = FileLoader.imageFile(filename)
        def stream = new FileInputStream(file)

        expect:
        thumbnailValidationService.validateThumbnailStream(stream) == valid

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
