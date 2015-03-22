package in.reeltime.playlist

import grails.test.mixin.TestFor
import in.reeltime.storage.PathGenerationService
import in.reeltime.storage.StorageService
import spock.lang.Specification

@TestFor(PlaylistAndSegmentStorageService)
class PlaylistAndSegmentStorageServiceSpec extends Specification {

    String path
    String outputBase

    PathGenerationService pathGenerationService
    StorageService storageService

    void setup() {
        path = 'some-file'

        outputBase = 'playlist-and-segments'
        service.playlistBase = outputBase

        pathGenerationService = Mock(PathGenerationService)
        service.pathGenerationService = pathGenerationService

        storageService = Mock(StorageService)
        service.storageService = storageService
    }

    void "generate unique playlist path"() {
        when:
        service.uniquePlaylistPath

        then:
        1 * pathGenerationService.generateRandomUniquePath(outputBase)
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
