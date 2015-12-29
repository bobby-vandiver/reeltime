package in.reeltime.thumbnail

import grails.test.mixin.TestFor
import in.reeltime.exceptions.ThumbnailGenerationException
import spock.lang.Specification
import spock.lang.Unroll

import javax.imageio.ImageIO

@TestFor(ThumbnailGenerationService)
class ThumbnailGenerationServiceSpec extends Specification {

    ThumbnailStorageService thumbnailStorageService

    void setup() {
        thumbnailStorageService = Mock(ThumbnailStorageService)
        service.thumbnailStorageService = thumbnailStorageService
    }

    @Unroll
    void "generate thumbnail at resolution [#resolution] and return path"() {
        given:
        def imagePath = 'test/files/images/small.png'
        def imageStream = new FileInputStream(imagePath)

        and:
        def expectedThumbnailPath = "${imagePath}-${resolution.name()}"

        when:
        def thumbnailPath = service.generateThumbnail(imagePath, resolution)

        then:
        thumbnailPath == expectedThumbnailPath

        and:
        1 * thumbnailStorageService.load(imagePath) >> imageStream

        1 * thumbnailStorageService.store(_, _) >> { args ->
            def storedStream = args[0] as InputStream
            def storedImage = ImageIO.read(storedStream)

            assert storedImage.width == resolution.width
            assert storedImage.height == resolution.height

            def storedPath = args[1] as String
            assert storedPath == expectedThumbnailPath
        }

        where:
        _   |   resolution
        _   |   ThumbnailResolution.RESOLUTION_1X
        _   |   ThumbnailResolution.RESOLUTION_2X
        _   |   ThumbnailResolution.RESOLUTION_3X
    }

    void "wrap and rethrow exceptions"() {
        given:
        ImageIO.metaClass.static.read = { InputStream ->
            throw new IOException('TEST')
        }

        when:
        service.generateThumbnail('anything', ThumbnailResolution.RESOLUTION_1X)

        then:
        def e = thrown(ThumbnailGenerationException)
        e.message == "Error occurred while generating thumbnail at resolution [RESOLUTION_1X] for image [anything]"

        cleanup:
        ImageIO.metaClass = null
    }
}
