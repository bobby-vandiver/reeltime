package in.reeltime.hls.playlist.parser

import spock.lang.Specification
import spock.lang.Unroll

class StreamAttributesParserSpec extends Specification {

    void "no stream attributes"() {
        when:
        def attributes = StreamAttributesParser.parseAttributes('')

        then:
        attributes == [:]
    }

    @Unroll
    void "one stream attribute [#text]"() {
        when:
        def attributes = StreamAttributesParser.parseAttributes(text)

        then:
        attributes == expected

        where:
        text                                |   expected
        'PROGRAM-ID=1'                      |   [programId: 1]
        'RESOLUTION=400x170'                |   [resolution: '400x170']
        'CODECS="avc1.42001e,mp4a.40.2"'    |   [codecs: 'avc1.42001e,mp4a.40.2']
        'BANDWIDTH=474000'                  |   [bandwidth: 474000]
    }

    @Unroll
    "multiple stream attributes [#text]"() {
        when:
        def attributes = StreamAttributesParser.parseAttributes(text)

        then:
        attributes == expected

        where:
        text                                                                                |   expected
        'PROGRAM-ID=1,RESOLUTION=400x170'                                                   |   [programId: 1, resolution: '400x170']
        'PROGRAM-ID=1,RESOLUTION=400x170,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=474000'   |   [programId: 1, resolution: '400x170', codecs: 'avc1.42001e,mp4a.40.2', bandwidth: 474000]
    }

    void "ignore unsupported attributes"() {
        expect:
        StreamAttributesParser.parseAttributes('UNSUPPORTED=IGNORED') == [:]
    }
}
