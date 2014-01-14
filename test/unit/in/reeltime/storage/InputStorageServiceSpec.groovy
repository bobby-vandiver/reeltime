package in.reeltime.storage

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(InputStorageService)
class InputStorageServiceSpec extends Specification {

    void "load storage input base path from config and store stream"() {
        given:
        def inputBase = 'master-videos'
        grailsApplication.config.reeltime.storage.input = inputBase

        and:
        service.grailsApplication = grailsApplication
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
