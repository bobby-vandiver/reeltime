package in.reeltime.hls.playlist.parser

import spock.lang.Specification
import spock.lang.Unroll

class MediaPlaylistParserSpec extends Specification {

    @Unroll
    void "minimal playlist must have one segment and target duration"() {
        given:
        def input = """#EXTM3U
                      |#EXT-X-TARGETDURATION:${targetDuration}
                      |#EXTINF:${segmentDuration},
                      |${segmentUri}""".stripMargin()

        def reader = new StringReader(input)

        when:
        def playlist = MediaPlaylistParser.parse(reader)

        then:
        playlist.targetDuration == targetDuration

        and:
        playlist.segments.size() == 1
        playlist.segments[0] == [(segmentUri): segmentDuration]

        where:
        targetDuration  |   segmentDuration |   segmentUri
        5220            |   '5220'          |   'http://media.example.com/entire.ts'
        1234            |   '1234'          |   'foobar.ts'
    }

    void "playlist contains multiple segments"() {
        given:
        def input = '''#EXTM3U
                      |#EXT-X-TARGETDURATION:8
                      |#EXT-X-MEDIA-SEQUENCE:2680
                      |#EXTINF:8,
                      |https://priv.example.com/fileSequence2680.ts
                      |#EXTINF:8,
                      |https://priv.example.com/fileSequence2681.ts
                      |#EXTINF:8,
                      |https://priv.example.com/fileSequence2682.ts'''.stripMargin()

        def reader = new StringReader(input)

        when:
        def playlist = MediaPlaylistParser.parse(reader)

        then:
        playlist.targetDuration == 8
        playlist.mediaSequence == 2680

        and:
        playlist.segments.size() == 3

        and:
        playlist.segments[0] == ['https://priv.example.com/fileSequence2680.ts': '8']
        playlist.segments[1] == ['https://priv.example.com/fileSequence2681.ts': '8']
        playlist.segments[2] == ['https://priv.example.com/fileSequence2682.ts': '8']
    }

    @Unroll
    void "playlist allow cache [#allowed]"() {
        given:
        def input = """#EXTM3U
                      |#EXT-X-TARGETDURATION:10
                      |#EXT-X-ALLOW-CACHE:${allowed}
                      |#EXTINF:10,
                      |foo.ts""".stripMargin()

        def reader = new StringReader(input)

        when:
        def playlist = MediaPlaylistParser.parse(reader)

        then:
        playlist.allowCache == expected

        where:
        allowed     |   expected
        'YES'       |   true
        'NO'        |   false
    }

    @Unroll
    void "playlist allow cache must be YES or NO not [#allowed]"() {
        given:
        def input = """#EXTM3U
                      |#EXT-X-TARGETDURATION:10
                      |#EXT-X-ALLOW-CACHE:${allowed}
                      |#EXTINF:10,
                      |foo.ts""".stripMargin()

        def reader = new StringReader(input)

        when:
        MediaPlaylistParser.parse(reader)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == '#EXT-X-ALLOW-CACHE must be YES or NO'

        where:
        allowed << ['yes', 'yES', 'Yes', 'No', 'nO', 'no', 'PLZ']
    }

    @Unroll
    void "playlist specifies protocol version [#version]"() {
        given:
        def input = """#EXTM3U
                      |#EXT-X-VERSION:${version}
                      |#EXT-X-TARGETDURATION:12
                      |#EXTINF:11.308056,
                      |hls-spidey00000.ts""".stripMargin()

        def reader = new StringReader(input)

        when:
        def playlist = MediaPlaylistParser.parse(reader)

        then:
        playlist.version == version

        where:
        version << [1, 2, 3]
    }

    void "store segment durations as strings to avoid floating point round off"() {
        given:
        def input = '''#EXTM3U
                      |#EXT-X-VERSION:3
                      |#EXT-X-MEDIA-SEQUENCE:0
                      |#EXT-X-ALLOW-CACHE:YES
                      |#EXT-X-TARGETDURATION:12
                      |#EXTINF:11.308056,
                      |hls-spidey00000.ts
                      |#EXTINF:11.262022,
                      |hls-spidey00001.ts
                      |#EXT-X-ENDLIST'''.stripMargin()

        def reader = new StringReader(input)

        when:
        def playlist = MediaPlaylistParser.parse(reader)

        then:
        playlist.version == 3
        playlist.mediaSequence == 0
        playlist.allowCache == true
        playlist.targetDuration == 12

        and:
        playlist.segments.size() == 2
        playlist.segments[0] == ['hls-spidey00000.ts': '11.308056']
        playlist.segments[1] == ['hls-spidey00001.ts': '11.262022']
    }
}
