package in.reeltime.metadata

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class StreamMetadataSpec extends Specification {

    @Unroll
    void "duration [#duration] is valid [#valid]"() {
        given:
        def stream = new StreamMetadata(duration: duration)

        expect:
        stream.validate() == valid

        where:
        duration        |   valid
        null            |   false
        ''              |   false
        '.0124'         |   false
        '124.'          |   false
        '9000.asf'      |   false
        '1234#1441'     |   false
        '9000.000000'   |   true
        '9000.000001'   |   true
        '9000.123456'   |   true
        '8999.999999'   |   true
        '1.0'           |   true
        '0.0'           |   true
    }

    @Unroll
    void "duration [#duration] rounds up to nearest duration in seconds [#expected]"() {
        given:
        def stream = new StreamMetadata(duration: duration)

        expect:
        stream.durationInSeconds == expected

        where:
        duration        |   expected
        null            |   null
        ''              |   null
        '.0124'         |   null
        '124.'          |   null
        '9000.asf'      |   null
        '1234#1441'     |   null
        '9000.000000'   |   9000
        '9000.000001'   |   9001
        '9000.123456'   |   9001
        '8999.999999'   |   9000
        '1.0'           |   1
        '0.0'           |   0
    }
}
