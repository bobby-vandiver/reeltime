package in.reeltime.hls.playlist.composer

import in.reeltime.hls.playlist.StreamAttributes
import spock.lang.Specification

class VariantPlaylistComposerSpec extends Specification {

    void "no streams"() {
        given:
        def writer = new StringWriter()

        when:
        VariantPlaylistComposer.compose([], writer)

        then:
        writer.toString() == '''#EXTM3U
                                |'''.stripMargin()
    }

    void "single stream variant"() {
        given:
        def stream = new StreamAttributes(
                uri: 'hls-bats-400k.m3u8',
                bandwidth: 455000,
                programId: 1,
                codecs: 'avc1.42001e,mp4a.40.2',
                resolution:'400x226'
        )

        def writer = new StringWriter()

        when:
        VariantPlaylistComposer.compose([stream], writer)

        then:
        writer.toString() == '''#EXTM3U
                               |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x226,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=455000
                               |hls-bats-400k.m3u8
                               |'''.stripMargin()
    }

    void "multiple stream variants"() {
        given:
        def stream1 = new StreamAttributes(
                uri: 'hls-bats-400k.m3u8',
                bandwidth: 455000,
                programId: 1,
                codecs: 'avc1.42001e,mp4a.40.2',
                resolution:'400x226'
        )

        def stream2 = new StreamAttributes(
                uri: 'hls-bats-600k.m3u8',
                bandwidth: 663000,
                programId: 1,
                codecs: 'avc1.42001e,mp4a.40.2',
                resolution:'480x270'
        )

        def writer = new StringWriter()

        when:
        VariantPlaylistComposer.compose([stream1, stream2], writer)

        then:
        writer.toString() == '''#EXTM3U
                               |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x226,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=455000
                               |hls-bats-400k.m3u8
                               |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=480x270,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=663000
                               |hls-bats-600k.m3u8
                               |'''.stripMargin()
    }
}
