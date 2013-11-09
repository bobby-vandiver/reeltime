package in.reeltime.video

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Playlist)
@Mock(Segment)
class PlaylistSpec extends Specification {

    @Unroll
    void "playlist contains [#count] ordered segments"() {
        given:
        def segments = createOrderedSegments(count)

        when:
        def playlist = new Playlist(segments: segments)

        then:
        playlist.segments.size() == count

        and:
        playlist.validate()

        where:
        count << [0, 1, 4]
    }

    void "unordered segments become ordered"() {
        given:
        def segments = [
                new Segment(segmentId: 1),
                new Segment(segmentId: 0)
        ]

        when:
        def playlist = new Playlist(segments: segments)

        then:
        playlist.segments*.segmentId == [0, 1]

        and:
        playlist.validate()
    }

    @Unroll
    void "length returns number of segments [#count]"() {
        given:
        def segments = createOrderedSegments(count)

        when:
        def playlist = new Playlist(segments: segments)

        then:
        playlist.length == count

        where:
        count << [0, 3, 6]
    }

    private static createOrderedSegments(count) {
        (0..<count).collect { id -> new Segment(segmentId: id) }
    }
}
