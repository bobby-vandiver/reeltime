package in.reeltime.playlist

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.common.AbstractCommandSpec
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class PlaylistCommandSpec extends AbstractCommandSpec {

    @Unroll
    void "playlistId [#playlistId] is valid [#valid]"() {
        expect:
        assertCommandFieldIsValid(PlaylistCommand, 'playlist_id', playlistId, valid, code)

        where:
        playlistId  |   valid   |   code
        null        |   false   |   'nullable'
        -1          |   true    |   null
        0           |   true    |   null
        1           |   true    |   null
    }
}
