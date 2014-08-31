package in.reeltime.playlist

import grails.test.mixin.TestFor
import in.reeltime.video.Video
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(PlaylistUri)
class PlaylistUriSpec extends Specification {

    @Unroll
    void "uri cannot be [#value]"() {
        given:
        def uri = new PlaylistUri(uri: value)

        expect:
        !uri.validate(['uri'])

        where:
        _   |   value
        _   |   null
        _   |   ''
    }

    void "uri not associated with a video"() {
        given:
        def uri = new PlaylistUri(video: null)

        expect:
        !uri.validate(['video'])
    }

    void "must be associated with a video"() {
        given:
        def uri = new PlaylistUri(video: new Video())

        expect:
        uri.validate(['video'])
    }
}
