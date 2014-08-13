package in.reeltime.reel

import grails.test.spock.IntegrationSpec

class AudienceServiceIntegrationSpec extends IntegrationSpec {

    def audienceService

    void "create audience has no users"() {
        when:
        def audience = audienceService.createAudience()

        then:
        audience.members.size() == 0
    }
}
