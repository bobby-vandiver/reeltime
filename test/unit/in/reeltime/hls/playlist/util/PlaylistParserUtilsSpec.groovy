package in.reeltime.hls.playlist.util

import spock.lang.Specification
import spock.lang.Unroll

class PlaylistParserUtilsSpec extends Specification {

    void "first line must be #EXTM3U"() {
        given:
        def reader = new StringReader('')

        when:
        PlaylistParserUtils.ensureExtendedM3U(reader)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'First line of playlist must be #EXTM3U'
    }

    void "read and ignore first line when it is #EXTM3U"() {
        given:
        def reader = new StringReader('#EXTM3U')

        when:
        PlaylistParserUtils.ensureExtendedM3U(reader)

        then:
        notThrown(Exception)
    }

    @Unroll
    void "getTagAndParams returns [#expectedTag] and [#expectedParams] for line [#line]"() {
        when:
        def (tag, params) = PlaylistParserUtils.getTagAndParams(line)

        then:
        tag == expectedTag
        params == expectedParams

        where:
        expectedTag         |   expectedParams                                      |   line
        '#EXTINF'           |   '11.262022,'                                        |   '#EXTINF:11.262022,'
        ' #EXTINF'          |   '11.262022,'                                        |   ' #EXTINF:11.262022,'
        '#EXT-X-VERSION'    |   '3'                                                 |   '#EXT-X-VERSION:3'
        '#EXT-X-ENDLIST'    |   null                                                |   '#EXT-X-ENDLIST'
        '#EXT-X-STREAM-INF' |   'PROGRAM-ID=1,RESOLUTION=400x170,BANDWIDTH=474000'  |   '#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x170,BANDWIDTH=474000'
    }
}
