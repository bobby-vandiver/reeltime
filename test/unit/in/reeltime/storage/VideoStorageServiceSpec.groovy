package in.reeltime.storage

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(VideoStorageService)
class VideoStorageServiceSpec extends Specification {

    void "load storage input base path from config and store stream"() {
        given:
        def inputBase = 'master-videos'
        service.videoBase = inputBase

        and:
        service.storageService = Mock(StorageService)

        and:
        def inputStream = new ByteArrayInputStream('foo'.bytes)
        def path = 'some-video'

        when:
        service.store(inputStream, path)

        then:
        1 * service.storageService.store(inputStream, inputBase, path)
    }
}
