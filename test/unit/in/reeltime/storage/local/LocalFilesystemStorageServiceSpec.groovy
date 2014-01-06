package in.reeltime.storage.local

import grails.test.mixin.TestFor
import in.reeltime.storage.StorageService
import org.apache.commons.logging.Log
import spock.lang.Specification

@TestFor(LocalFilesystemStorageService)
class LocalFilesystemStorageServiceSpec extends Specification {

    void "LocalFilesystemStorageService must be an instance of StorageService"() {
        expect:
        service instanceof StorageService
    }

    void "basePath is the directory and resourcePath is the filename"() {
        given:
        def directory = System.getProperty('java.io.tmpdir') + File.separator + UUID.randomUUID()
        def filename = 'test.txt'

        def contents = 'THIS IS A TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        when:
        service.store(inputStream, directory, filename)

        then:
        assertFileContents(contents, directory, filename)
    }

    private static void assertFileContents(String contents, String directory, String filename) {
        new File(directory, filename).withInputStream { assert it.bytes == contents.bytes }
    }

    void "log directory and filename"() {
        given:
        service.log = Mock(Log)

        and:
        def directory = System.getProperty('java.io.tmpdir') + File.separator + UUID.randomUUID()
        def filename = 'test.txt'

        def contents = 'THIS IS A TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        when:
        service.store(inputStream, directory, filename)

        then:
        1 * service.log.debug("Creating directory [$directory]")
        1 * service.log.debug("Creating file [$filename]")
    }
}
