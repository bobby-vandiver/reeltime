package in.reeltime.storage

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(OutputStorageService)
class OutputStorageServiceSpec extends Specification {

    void "load storage output base path from config and load stream"() {
        given:
        def outputBase = 'playlist-and-segments'
        service.outputBase = outputBase

        and:
        service.storageService = Mock(StorageService)

        and:
        def contents = 'TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        and:
        def path = 'some-file'

        when:
        def stream = service.load(path)

        then:
        1 * service.storageService.load(outputBase, path) >> inputStream

        and:
        stream.text == contents
    }
}
