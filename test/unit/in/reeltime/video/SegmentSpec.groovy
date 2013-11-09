package in.reeltime.video

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Segment)
class SegmentSpec extends Specification {

    private static final String IGNORE_LOCATION = 'ignored'
    private static final String IGNORE_DURATION = '1.0'

    @Unroll
    void "segmentId [#value] is [#valid]"() {
        when:
        def segment = new Segment(segmentId: value, location: IGNORE_LOCATION, duration: IGNORE_DURATION)

        then:
        segment.validate() == valid

        where:
        value       |   valid
        0           |   true
        234         |   true
        -1          |   false
        'foo'       |   false
        ''          |   false
    }

    @Unroll
    void "location [#path] is [#valid]"() {
        when:
        def segment = new Segment(location: path, duration: IGNORE_DURATION)

        then:
        segment.validate() == valid

        where:
        path                            |   valid
        null                            |   false
        ''                              |   false
        '/tmp/foo'                      |   true
        'http://www.foo.com/bar/baz'    |   true
    }

    @Unroll
    void "duration [#length] is [#valid]"() {
        when:
        def segment = new Segment(duration: length, location: IGNORE_LOCATION)

        then:
        segment.validate() == valid

        where:
        length      |   valid
        '0'         |   true
        '2.945244'  |   true
        '11.071933' |   true
        '12.2341'   |   true
        '001.41'    |   true
        '-0.0001'   |   false
        '-1.0'      |   false
        null        |   false
        ''          |   false
        '.001'      |   false
        'foo'       |   false
        '1.foo'     |   false
        '1.'        |   false
    }

    @Unroll
    void "compare [#leftId] to [#rightId] returns [#result]"() {
        given:
        def leftSegment = createSegmentFromId(leftId)
        def rightSegment = createSegmentFromId(rightId)

        expect:
        leftSegment.compareTo(rightSegment) == result

        where:
        leftId  |   rightId     |   result
        0       |   1           |   -1
        1       |   1           |   0
        1       |   0           |   1
    }

    private static Segment createSegmentFromId(int id) {
        new Segment(segmentId: id, duration: IGNORE_DURATION, location: IGNORE_LOCATION)
    }
}
