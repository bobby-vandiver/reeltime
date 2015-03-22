package in.reeltime.thumbnail

import grails.test.mixin.TestFor
import in.reeltime.storage.PathGenerationService
import in.reeltime.storage.StorageService
import spock.lang.Specification

@TestFor(ThumbnailStorageService)
class ThumbnailStorageServiceSpec extends Specification {

    PathGenerationService pathGenerationService
    StorageService storageService

    String thumbnailBase

    String contents
    InputStream inputStream
    String path

    void setup() {
        thumbnailBase = 'thumbnails'
        service.thumbnailBase = thumbnailBase

        pathGenerationService = Mock(PathGenerationService)
        service.pathGenerationService = pathGenerationService

        storageService = Mock(StorageService)
        service.storageService = storageService

        contents = 'test'
        inputStream = new ByteArrayInputStream(contents.bytes)
        path = 'some-thumbnail'
    }

    void "generate unique thumbnail path"() {
        when:
        service.uniqueThumbnailPath

        then:
        1 * pathGenerationService.generateRandomUniquePath(thumbnailBase)
    }

    void "load thumbnail at path"() {
        when:
        def stream = service.load(path)

        then:
        stream.text == contents

        and:
        1 * storageService.load(thumbnailBase, path) >> inputStream
    }

    void "store thumbnail at path"() {
        when:
        service.store(inputStream, path)

        then:
        1 * storageService.store(inputStream, thumbnailBase, path)
    }
}
