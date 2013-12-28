package in.reeltime.video.playlist

import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Segment)
@Build([Segment, Playlist])
class SegmentSpec extends Specification {

    private static final String IGNORE_LOCATION = 'ignored'
    private static final String IGNORE_DURATION = '1.0'
    private static final Playlist IGNORE_PLAYLIST = new Playlist()

    private Map args = [location: IGNORE_LOCATION, duration: IGNORE_DURATION, playlist: IGNORE_PLAYLIST]

    void "playlist cannot be null"() {
        given:
        args.playlist = null

        when:
        def segment = new Segment(args)

        then:
        !segment.validate()

        and:
        segment.errors['playlist'].code == 'nullable'
    }

    void "segment must belong to a playlist"() {
        given:
        def playlist = Playlist.build()
        args << [playlist: playlist]

        when:
        def segment = new Segment(args)

        then:
        segment.validate()

        and:
        segment.playlist == playlist
    }

    @Unroll
    void "segmentId [#value] is [#valid]"() {
        given:
        args << [segmentId: value]

        when:
        def segment = new Segment(args)

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
        given:
        args << [location: path]

        when:
        def segment = new Segment(args)

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
        given:
        args << [duration: length]

        when:
        def segment = new Segment(args)

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
        def leftSegment = Segment.build(segmentId: leftId)
        def rightSegment = Segment.build(segmentId: rightId)

        expect:
        leftSegment.compareTo(rightSegment) == result

        where:
        leftId  |   rightId     |   result
        0       |   1           |   -1
        1       |   1           |   0
        1       |   0           |   1
    }
}
