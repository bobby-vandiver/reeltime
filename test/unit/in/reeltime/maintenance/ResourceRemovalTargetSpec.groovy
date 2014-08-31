package in.reeltime.maintenance

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ResourceRemovalTarget)
class ResourceRemovalTargetSpec extends Specification {

    @Unroll
    void "uri cannot be [#value]"() {
        given:
        def target = new ResourceRemovalTarget(uri: value)

        expect:
        !target.validate(['uri'])

        where:
        _   |   value
        _   |   null
        _   |   ''
    }

    void "uri must be unique"() {
        given:
        def existing = new ResourceRemovalTarget(uri: 'somewhere')
        mockForConstraintsTests(ResourceRemovalTarget, [existing])

        when:
        def target = new ResourceRemovalTarget(uri: 'somewhere')

        then:
        !target.validate(['uri'])
    }
}
