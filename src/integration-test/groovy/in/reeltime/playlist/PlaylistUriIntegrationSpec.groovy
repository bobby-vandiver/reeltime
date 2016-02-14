package in.reeltime.playlist

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class PlaylistUriIntegrationSpec extends Specification {

    void "uri must be unique"() {
        given:
        new PlaylistUri(uri: 'somewhere', type: PlaylistType.Media).save()

        when:
        def uri = new PlaylistUri(uri: 'somewhere')

        then:
        !uri.validate(['uri'])
    }
}
