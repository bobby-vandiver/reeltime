package in.reeltime.playlist

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.common.AbstractCommandSpec
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class SegmentCommandSpec extends AbstractCommandSpec {

    @Unroll
    void "segmentId [#segmentId] is valid [#valid]"() {
        expect:
        assertCommandFieldIsValid(SegmentCommand, 'segment_id', segmentId, valid, code)

        where:
        segmentId   |   valid   |   code
        null        |   false   |   'nullable'
        -1          |   true    |   null
        0           |   true    |   null
        1           |   true    |   null
    }
}
