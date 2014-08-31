package in.reeltime.maintenance

import grails.test.spock.IntegrationSpec

class ResourceRemovalServiceIntegrationSpec extends IntegrationSpec {

    def resourceRemovalService

    void "schedule new resource for removal"() {
        when:
        resourceRemovalService.scheduleForRemoval('parent', 'child')

        then:
        ResourceRemovalTarget.findByBaseAndRelative('parent', 'child') != null
    }
}
