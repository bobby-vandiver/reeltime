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
        grailsApplication.config.reeltime.metadata.maxDurationInSeconds = 9000

        and:
        def stream = new StreamMetadata(duration: duration)

        expect:
        stream.validate() == valid

        where:
        duration        |   valid
        ''              |   false
        '.0124'         |   false
        '124.'          |   false
        '9000.asf'      |   false
        '1234#1441'     |   false
        '9000.000000'   |   false
        '9000.000001'   |   false
        '9000.123456'   |   false
        '8999.999999'   |   true
        '1.0'           |   true
        '0.0'           |   true
    }
}
