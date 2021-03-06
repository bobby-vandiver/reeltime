package in.reeltime.playlist

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(PlaylistUri)
class PlaylistUriSpec extends Specification {

    @Unroll
    void "[#key] cannot be [#value]"() {
        given:
        def uri = new PlaylistUri((key): value)

        expect:
        !uri.validate([key])

        where:
        key     |   value
        'uri'   |   null
        'uri'   |   ''
        'type'  |   null
    }

    @Unroll
    void "valid type [#type.name()] specified"() {
        given:
        def uri = new PlaylistUri(type: type)

        expect:
        uri.validate(['type'])

        where:
        _   |   type
        _   |   PlaylistType.Variant
        _   |   PlaylistType.Media
    }
}
