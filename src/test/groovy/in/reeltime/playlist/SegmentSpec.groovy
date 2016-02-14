package in.reeltime.playlist

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Segment)
class SegmentSpec extends Specification {

    @Unroll
    void "segmentId [#value] is [#valid]"() {
        when:
        def segment = new Segment(segmentId: value)

        then:
        segment.validate(['segmentId']) == valid

        where:
        value       |   valid
        0           |   true
        234         |   true
        -1          |   false
        'foo'       |   false
        ''          |   false
    }

    @Unroll
    void "uri [#path] is [#valid]"() {
        when:
        def segment = new Segment(uri: path)

        then:
        segment.validate(['uri']) == valid

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
        def segment = new Segment(duration: length)

        then:
        segment.validate(['duration']) == valid

        where:
        length      |   valid
        '0'         |   true
        '1.0'       |   true
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
        when:
        def leftSegment = new Segment(segmentId: leftId).save(validate: false)
        def rightSegment = new Segment(segmentId: rightId).save(validate: false)

        then:
        leftSegment.compareTo(rightSegment) == result

        where:
        leftId  |   rightId     |   result
        0       |   1           |   -1
        1       |   1           |   0
        1       |   0           |   1
    }
}
