package in.reeltime.storage

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(VideoStorageService)
class VideoStorageServiceSpec extends Specification {

    void "load storage input base path from config and store stream"() {
        given:
        def inputBase = 'master-videos'
        grailsApplication.config.storage.input = inputBase

        and:
        service.grailsApplication = grailsApplication
        service.storageService = Mock(StorageService)

        and:
        def videoStream = new ByteArrayInputStream('foo'.bytes)
        def path = 'some-video'

        when:
        service.storeVideoStream(videoStream, path)

        then:
        1 * service.storageService.store(videoStream, inputBase, path)
    }
}
