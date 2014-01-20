package in.reeltime.playlist

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Playlist)
class PlaylistSpec extends Specification {

    void "playlist requires HLS stream information"() {
        when:
        def playlist = new Playlist(programId: 1, resolution: '400x170', codecs: 'avc1.42001e,mp4a.40.2', bandwidth: 474000)

        then:
        playlist.validate(['programId', 'resolution', 'codecs', 'bandwidth'])

        and:
        playlist.programId == 1
        playlist.resolution == '400x170'
        playlist.codecs == 'avc1.42001e,mp4a.40.2'
        playlist.bandwidth == 474000
    }

    void "playlist includes HLS playback metadata"() {
        when:
        def playlist = new Playlist(hlsVersion: 3, mediaSequence: 0, targetDuration: 12)

        then:
        playlist.validate(['hlsVersion', 'mediaSequence', 'targetDuration'])

        and:
        playlist.hlsVersion == 3
        playlist.mediaSequence == 0
        playlist.targetDuration == 12
    }

    void "resolution and codecs can be blank"() {
        when:
        def playlist = new Playlist(codecs: '', resolution: '')

        then:
        playlist.validate(['codecs', 'resolution'])
    }

    void "resolution and codecs can be null"() {
        when:
        def playlist = new Playlist(codecs: null, resolution: null)

        then:
        playlist.validate(['codecs', 'resolution'])
    }

    @Unroll
    void "playlist contains [#count] ordered segments"() {
        given:
        def segments = createOrderedSegments(count)

        when:
        def playlist = new Playlist(segments: segments)

        then:
        playlist.segments.size() == count

        and:
        playlist.validate(['segments'])

        where:
        count << [0, 1, 4]
    }

    void "unordered segments become ordered"() {
        given:
        def segments = [
                new Segment(segmentId: 1, uri: 'first', duration: '1.0'),
                new Segment(segmentId: 0, uri: 'zeroth', duration: '1.0')
        ]

        when:
        def playlist = new Playlist(segments: segments)

        then:
        playlist.segments*.segmentId == [0, 1]

        and:
        playlist.validate(['segments'])
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
        (0..<count).collect { id -> new Segment(segmentId: id, uri: "seg-$id", duration: '1.0') }
    }
}
