package in.reeltime.thumbnail

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class ThumbnailIntegrationSpec extends Specification {

    void "uri must be unique"() {
        given:
        new Thumbnail(uri: 'somewhere', resolution: ThumbnailResolution.RESOLUTION_1X).save()

        when:
        def thumbnail = new Thumbnail(uri: 'somewhere')

        then:
        !thumbnail.validate(['uri'])
    }
}
