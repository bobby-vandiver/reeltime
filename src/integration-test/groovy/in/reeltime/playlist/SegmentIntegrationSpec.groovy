package in.reeltime.playlist

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class SegmentIntegrationSpec extends Specification {

    void "uri must be unique"() {
        given:
        new Segment(uri: 'somewhere', segmentId: 0, duration: '1.4').save()

        when:
        def segment = new Segment(uri: 'somewhere')

        then:
        !segment.validate(['uri'])
    }
}
