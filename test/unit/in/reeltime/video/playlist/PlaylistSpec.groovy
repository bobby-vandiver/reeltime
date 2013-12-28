package in.reeltime.video.playlist

import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import in.reeltime.video.Video
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Playlist)
@Build(Segment)
class PlaylistSpec extends Specification {

    private static final IGNORE_VIDEO = new Video()

    @Unroll
    void "playlist contains [#count] ordered segments"() {
        given:
        def segments = createOrderedSegments(count)

        when:
        def playlist = new Playlist(segments: segments, video: IGNORE_VIDEO)

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
                Segment.build(segmentId: 1),
                Segment.build(segmentId: 0)
        ]

        when:
        def playlist = new Playlist(segments: segments, video: IGNORE_VIDEO)

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
        (0..<count).collect { id -> Segment.build(segmentId: id) }
    }
}
