package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.storage.PathGenerationService
import in.reeltime.storage.StorageService
import spock.lang.Specification

@TestFor(VideoStorageService)
class VideoStorageServiceSpec extends Specification {

    PathGenerationService pathGenerationService
    StorageService storageService

    String inputBase

    void setup() {
        inputBase = 'master-videos'
        service.videoBase = inputBase

        pathGenerationService = Mock(PathGenerationService)
        service.pathGenerationService = pathGenerationService

        storageService = Mock(StorageService)
        service.storageService = storageService
    }

    void "generate unique video path"() {
        when:
        service.uniqueVideoPath

        then:
        1 * pathGenerationService.generateRandomUniquePath(inputBase)
    }

    void "load storage input base path from config and store stream"() {
        given:
        def inputStream = new ByteArrayInputStream('foo'.bytes)
        def path = 'some-video'

        when:
        service.store(inputStream, path)

        then:
        1 * storageService.store(inputStream, inputBase, path)
    }
}
