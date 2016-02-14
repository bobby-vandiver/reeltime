package in.reeltime.maintenance

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class ResourceRemovalServiceIntegrationSpec extends Specification {

    @Autowired
    ResourceRemovalService resourceRemovalService

    void "schedule new resource for removal"() {
        when:
        resourceRemovalService.scheduleForRemoval('parent', 'child')

        then:
        ResourceRemovalTarget.findByBaseAndRelative('parent', 'child') != null
    }
}
