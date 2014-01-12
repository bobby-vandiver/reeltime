package in.reeltime.storage.local

import grails.test.mixin.TestFor
import in.reeltime.storage.StorageService
import org.apache.commons.logging.Log
import spock.lang.Specification
import spock.lang.Unroll

import static java.io.File.separator

@TestFor(LocalFileSystemStorageService)
class LocalFileSystemStorageServiceSpec extends Specification {

    void "LocalFilesystemStorageService must be an instance of StorageService"() {
        expect:
        service instanceof StorageService
    }

    @Unroll
    void "file at [#path] exists"() {
        given:
        def directory = randomTempDirectory()
        def filename = path + 'test.txt'

        and:
        def inputStream = new ByteArrayInputStream('TEST'.bytes)
        service.store(inputStream, directory, filename)

        expect:
        service.exists(directory, filename)

        where:
        path << ['', 'foo' + separator + 'bar' + separator]
    }

    @Unroll
    void "file at [#path] does not exist"() {
        given:
        def directory = randomTempDirectory()
        def filename = path + 'test.txt'

        expect:
        !service.exists(directory, filename)

        where:
        path << ['', 'foo' + separator + 'bar' + separator]
    }

    void "base is the directory and relative is the filename"() {
        given:
        def directory = randomTempDirectory()
        def filename = 'test.txt'

        def contents = 'THIS IS A TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        when:
        service.store(inputStream, directory, filename)

        then:
        assertFileContents(contents, directory, filename)
    }

    void "relative specifies a nested file"() {
        given:
        def directory = randomTempDirectory()
        def filename = 'test.txt'

        def relativePath = 'foo' + separator + 'bar' + separator
        def filePath = relativePath + filename

        def absolutePath = directory + separator + relativePath

        and:
        def contents = 'THIS IS A TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        when:
        service.store(inputStream, directory, filePath)

        then:
        assertFileContents(contents, absolutePath, filename)
    }

    private static void assertFileContents(String contents, String directory, String filename) {

        println "***directory: $directory"
        println "***filename: $filename"

        new File(directory, filename).withInputStream { assert it.bytes == contents.bytes }
    }

    void "log directory and filename"() {
        given:
        service.log = Mock(Log)

        and:
        def directory = randomTempDirectory()
        def filename = 'test.txt'

        def contents = 'THIS IS A TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        when:
        service.store(inputStream, directory, filename)

        then:
        1 * service.log.debug("Creating directory [$directory]")
        1 * service.log.debug("Creating file [$filename]")
    }

    private static String randomTempDirectory() {
        System.getProperty('java.io.tmpdir') + separator + UUID.randomUUID()
    }
}
