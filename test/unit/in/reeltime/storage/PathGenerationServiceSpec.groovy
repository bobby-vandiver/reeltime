package in.reeltime.storage

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(PathGenerationService)
class PathGenerationServiceSpec extends Specification {

    private static final UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/

    @Unroll
    void "unique file generated the first time for [#configName]"() {
        given:
        service."${propertyName}" = base

        and:
        service.storageService = Mock(StorageService)

        when:
        def name = service."$methodName"()

        then:
        name.matches(UUID_REGEX)

        and:
        1 * service.storageService.exists(base, _) >> false

        where:
        propertyName    |   base        |   methodName
        'inputBase'     |   'inbox'     |   'getUniqueInputPath'
        'outputBase'    |   'outbox'    |   'getUniqueOutputPath'
    }

    @Unroll
    void "unique file generated after the second time for [#configName]"() {
        given:
        service."${propertyName}" = base

        and:
        service.storageService = Mock(StorageService)

        when:
        def name = service."$methodName"()

        then:
        name.matches(UUID_REGEX)

        and:
        2 * service.storageService.exists(base, _) >>> [true, false]

        where:
        propertyName    |   base        |   methodName
        'inputBase'     |   'inbox'     |   'getUniqueInputPath'
        'outputBase'    |   'outbox'    |   'getUniqueOutputPath'
    }
}
