package in.reeltime.storage.local

import grails.test.mixin.TestFor
import in.reeltime.storage.StorageService
import org.apache.commons.logging.Log
import spock.lang.Specification
import spock.lang.Unroll

import static java.io.File.separator

@TestFor(LocalFileSystemStorageService)
class LocalFileSystemStorageServiceSpec extends Specification {

    private String directory
    private String filename

    private String contents
    private InputStream inputStream

    void setup() {
        directory = System.getProperty('java.io.tmpdir') + separator + UUID.randomUUID()
        filename = 'test.txt'
        contents = 'THIS IS A TEST'
        inputStream = new ByteArrayInputStream(contents.bytes)
    }

    void "LocalFilesystemStorageService must be an instance of StorageService"() {
        expect:
        service instanceof StorageService
    }

    @Unroll
    void "file at [#path] exists"() {
        given:
        def filePath = path + 'test.txt'
        service.store(inputStream, directory, filePath)

        expect:
        service.exists(directory, filePath)

        where:
        path << ['', 'foo' + separator + 'bar' + separator]
    }

    @Unroll
    void "file at [#path] does not exist"() {
        given:
        def filePath = path + 'test.txt'

        expect:
        !service.exists(directory, filePath)

        where:
        path << ['', 'foo' + separator + 'bar' + separator]
    }

    void "base is the directory and relative is the filename"() {
        when:
        service.store(inputStream, directory, filename)

        then:
        assertFileContents(contents, directory, filename)
    }

    void "relative specifies a nested file"() {
        given:
        def relativePath = 'foo' + separator + 'bar' + separator
        def filePath = relativePath + filename

        and:
        def absolutePath = directory + separator + relativePath

        when:
        service.store(inputStream, directory, filePath)

        then:
        assertFileContents(contents, absolutePath, filename)
    }

    private static void assertFileContents(String contents, String directory, String filename) {
        new File(directory, filename).withInputStream { assert it.bytes == contents.bytes }
    }

    void "log directory and filename"() {
        given:
        service.log = Mock(Log)

        when:
        service.store(inputStream, directory, filename)

        then:
        1 * service.log.debug("Creating directory [$directory]")
        1 * service.log.debug("Creating file [$filename]")
    }
}
