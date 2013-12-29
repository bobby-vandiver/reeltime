package in.reeltime.hls.playlist.slurper

import spock.lang.Specification
import spock.lang.Unroll

class VariantPlaylistSlurperSpec extends Specification {

    private VariantPlaylistSlurper slurper

    void setup() {
        slurper = new VariantPlaylistSlurper()
    }

    void "parse playlist with one stream"() {
        given:
        def input = '''#EXTM3U
                      |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x170,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=474000
                      |hls-spidey.m3u8'''.stripMargin()

        def reader = new StringReader(input)

        when:
        def playlist = slurper.parse(reader)

        then:
        def stream = playlist.'hls-spidey.m3u8'

        and:
        stream.programId == 1
        stream.resolution == '400x170'
        stream.codecs == 'avc1.42001e,mp4a.40.2'
        stream.bandwidth == 474000
    }

    void "parse playlist with multiple streams"() {
        given:
        def input = '''#EXTM3U
                      |#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1280000
                      |http://example.com/low.m3u8
                      |#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2560000
                      |http://example.com/mid.m3u8
                      |#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=7680000
                      |http://example.com/hi.m3u8'''.stripMargin()

        def reader = new StringReader(input)

        when:
        def playlist = slurper.parse(reader)

        then:
        def lowStream = playlist.'http://example.com/low.m3u8'

        and:
        lowStream.programId == 1
        lowStream.bandwidth == 1280000

        and:
        def midStream = playlist.'http://example.com/mid.m3u8'

        and:
        midStream.programId == 1
        midStream.bandwidth == 2560000

        and:
        def hiStream = playlist.'http://example.com/hi.m3u8'

        and:
        hiStream.programId == 1
        hiStream.bandwidth == 7680000
    }

    void "no stream attributes"() {
        when:
        def attributes = VariantPlaylistSlurper.parseAttributes('')

        then:
        attributes == [:]
    }

    @Unroll
    void "one stream attribute [#text]"() {
        when:
        def attributes = VariantPlaylistSlurper.parseAttributes(text)

        then:
        attributes == expected

        where:
        text                                |   expected
        'PROGRAM-ID=1'                      |   [programId: 1]
        'RESOLUTION=400x170'                |   [resolution: '400x170']
        'CODECS="avc1.42001e,mp4a.40.2"'    |   [codecs: 'avc1.42001e,mp4a.40.2']
        'BANDWIDTH=474000'                  |   [bandwidth: 474000]
    }

    @Unroll "multiple stream attributes [#text]"() {
        when:
        def attributes = VariantPlaylistSlurper.parseAttributes(text)

        then:
        attributes == expected

        where:
        text                                                                                |   expected
        'PROGRAM-ID=1,RESOLUTION=400x170'                                                   |   [programId: 1, resolution: '400x170']
        'PROGRAM-ID=1,RESOLUTION=400x170,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=474000'   |   [programId: 1, resolution: '400x170', codecs: 'avc1.42001e,mp4a.40.2', bandwidth: 474000]
    }

    void "ignore unsupported attributes"() {
        expect:
        VariantPlaylistSlurper.parseAttributes('UNSUPPORTED=IGNORED') == [:]
    }
}
