package in.reeltime.maintenance

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ResourceRemovalTarget)
class ResourceRemovalTargetSpec extends Specification {

    @Unroll
    void "[#key] cannot be [#value]"() {
        given:
        def target = new ResourceRemovalTarget((key): value)

        expect:
        !target.validate([key])

        where:
        key         |   value
        'base'      |   null
        'base'      |   ''
        'relative'  |   null
        'relative'  |   ''
    }

    @Unroll
    void "existing [#existingBase:#existingRelative] and new [#newBase:#newRelative] is valid [#valid]"() {
        given:
        new ResourceRemovalTarget(base: existingBase, relative: existingRelative).save()

        when:
        def target = new ResourceRemovalTarget(base: newBase, relative: newRelative)

        then:
        target.validate() == valid

        where:
        existingBase    |   existingRelative    |   newBase     |   newRelative     |   valid
        'sameBase'      |   'diffRel1'          |   'sameBase'  |   'diffRel2'      |   true
        'diffBase1'     |   'sameRel'           |   'diffBase2' |   'sameRel'       |   true
        'sameBase'      |   'sameRel'           |   'sameBase'  |   'sameRel'       |   false
    }
}
