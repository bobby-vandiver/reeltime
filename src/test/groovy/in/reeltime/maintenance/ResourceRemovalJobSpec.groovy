package in.reeltime.maintenance

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class ResourceRemovalJobSpec extends Specification {

    ResourceRemovalJob job
    ResourceRemovalService resourceRemovalService

    private static final int NUMBER_OF_TARGETS = 42

    void setup() {
        resourceRemovalService = Mock(ResourceRemovalService)
        job = new ResourceRemovalJob()

        job.resourceRemovalService = resourceRemovalService
        job.numberToRemovePerExecution = NUMBER_OF_TARGETS
    }

    void "job invokes removal service"() {
        when:
        job.execute()

        then:
        1 * resourceRemovalService.executeScheduledRemovals(NUMBER_OF_TARGETS)
    }
}
