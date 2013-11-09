package in.reeltime.video

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Segment)
class SegmentSpec extends Specification {

    @Unroll
    void "segmentId [#value] is [#valid]"() {
        when:
        def segment = new Segment(segmentId: value, location: 'foo')

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
        def segment = new Segment(location: path)

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
        def segment = new Segment(duration: length, location: 'foo')

        then:
        segment.validate() == valid

        where:
        length      |   valid
        0           |   true
        2.945244    |   true
        11.071933   |   true
        12.2341     |   true
        -0.0001     |   false
        -1.0        |   false
    }
}
