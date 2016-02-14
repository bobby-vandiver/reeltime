package in.reeltime.hls.playlist.parser

import spock.lang.Specification

class VariantPlaylistParserSpec extends Specification {

    void "parse playlist with one stream"() {
        given:
        def input = '''#EXTM3U
                      |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x170,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=474000
                      |hls-spidey.m3u8'''.stripMargin()

        def reader = new StringReader(input)

        when:
        def playlist = VariantPlaylistParser.parse(reader)

        then:
        playlist.streams.size() == 1

        and:
        playlist.streams[0].uri == 'hls-spidey.m3u8'
        playlist.streams[0].programId == 1
        playlist.streams[0].resolution == '400x170'
        playlist.streams[0].codecs == 'avc1.42001e,mp4a.40.2'
        playlist.streams[0].bandwidth == 474000
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
        def playlist = VariantPlaylistParser.parse(reader)

        then:
        playlist.streams.size() == 3

        and:
        playlist.streams[0].uri == 'http://example.com/low.m3u8'
        playlist.streams[0].programId == 1
        playlist.streams[0].bandwidth == 1280000

        and:
        playlist.streams[1].uri == 'http://example.com/mid.m3u8'
        playlist.streams[1].programId == 1
        playlist.streams[1].bandwidth == 2560000

        and:
        playlist.streams[2].uri == 'http://example.com/hi.m3u8'
        playlist.streams[2].programId == 1
        playlist.streams[2].bandwidth == 7680000
    }
}
