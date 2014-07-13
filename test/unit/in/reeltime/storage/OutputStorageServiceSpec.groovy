package in.reeltime.storage

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(OutputStorageService)
class OutputStorageServiceSpec extends Specification {

    String path
    String outputBase

    StorageService storageService

    void setup() {
        path = 'some-file'

        outputBase = 'playlist-and-segments'
        service.outputBase = outputBase

        storageService = Mock(StorageService)
        service.storageService = storageService
    }

    void "load storage output base path from config and load stream"() {
        given:
        def contents = 'TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        when:
        def stream = service.load(path)

        then:
        1 * storageService.load(outputBase, path) >> inputStream

        and:
        stream.text == contents
    }

    void "check existence of file in output storage"() {
        when:
        def actual = service.exists(path)

        then:
        actual == expected

        and:
        1 * storageService.exists(outputBase, path) >> expected

        where:
        _   |   expected
        _   |   false
        _   |   true
    }
}
