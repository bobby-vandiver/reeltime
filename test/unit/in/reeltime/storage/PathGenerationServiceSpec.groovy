package in.reeltime.storage

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(PathGenerationService)
class PathGenerationServiceSpec extends Specification {

    private static final UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/

    StorageService storageService

    void setup() {
        storageService = Mock(StorageService)
        service.storageService = storageService
        service.maxRetries = 5
    }

    void "unique file generated the first time"() {
        when:
        def name = service.generateRandomUniquePath('base')

        then:
        name.matches(UUID_REGEX)

        and:
        1 * storageService.exists('base', _) >> false
    }

    void "unique file generated after the second time"() {
        when:
        def name = service.generateRandomUniquePath('base')

        then:
        name.matches(UUID_REGEX)

        and:
        2 * storageService.exists('base', _) >>> [true, false]
    }

    void "unable to generate random path"() {
        when:
        service.generateRandomUniquePath('unknown')

        then:
        def e = thrown(IllegalStateException)
        e.message == 'Path generation for base [unknown] exceeded max retries'

        and:
        storageService.exists('unknown', _) >> true
    }
}
