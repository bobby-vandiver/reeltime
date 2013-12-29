package in.reeltime.hls.playlist.util

import spock.lang.Specification

class PlaylistSlurperUtilsSpec extends Specification {

    void "first line must be #EXTM3U"() {
        given:
        def reader = new StringReader('')

        when:
        PlaylistSlurperUtils.ensureExtendedM3U(reader)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'First line of playlist must be #EXTM3U'
    }

    void "read and ignore first line when it is #EXTM3U"() {
        given:
        def reader = new StringReader('#EXTM3U')

        when:
        PlaylistSlurperUtils.ensureExtendedM3U(reader)

        then:
        notThrown(Exception)
    }

    void "check tag returns [#truth] when checking for tag [tag] on line [#line]"() {
        expect:
        PlaylistSlurperUtils.checkTag(line, tag) == truth

        where:
        truth       |   tag         |   line
        true        |   '#EXTINF'   |   '#EXTINF:11.262022'
        false       |   '#EXTINF'   |   ' #EXTINF:11.262022'
        false       |   '#EXTINF'   |   '#EXT-X-VERSION:3'
    }
}
