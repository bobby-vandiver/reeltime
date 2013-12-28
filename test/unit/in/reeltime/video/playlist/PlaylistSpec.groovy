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

    private static final IGNORE_CODECS = 'ignoreCodecs'
    private static final IGNORE_RESOLUTION = 'ignoreResolution'

    private Map args = [ video: IGNORE_VIDEO, codecs: IGNORE_CODECS, resolution: IGNORE_RESOLUTION ]

    void "playlist requires HLS stream information"() {
        given:
        args << [programId: 1, resolution: '400x170', codecs: 'avc1.42001e,mp4a.40.2', bandwidth: 474000]

        when:
        def playlist = new Playlist(args)

        then:
        playlist.validate()

        and:
        playlist.programId == 1
        playlist.resolution == '400x170'
        playlist.codecs == 'avc1.42001e,mp4a.40.2'
        playlist.bandwidth == 474000
    }

    void "playlist includes HLS playback metadata"() {
        given:
        args << [hlsVersion: 3, mediaSequence: 0, targetDuration: 12]

        when:
        def playlist = new Playlist(args)

        then:
        playlist.validate()

        and:
        playlist.hlsVersion == 3
        playlist.mediaSequence == 0
        playlist.targetDuration == 12
    }

    @Unroll
    void "playlist contains [#count] ordered segments"() {
        given:
        def segments = createOrderedSegments(count)
        args << [segments: segments]

        when:
        def playlist = new Playlist(args)

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
        args << [segments: segments]

        when:
        def playlist = new Playlist(args)

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
