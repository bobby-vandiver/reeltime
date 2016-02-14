package in.reeltime.status

import in.reeltime.test.rest.RestRequest
import in.reeltime.test.spec.FunctionalSpec

class HealthCheckFunctionalSpec extends FunctionalSpec {

    void "health check endpoint is accessible without authentication"() {
        given:
        def request = new RestRequest(url: urlFactory.healthCheckUrl)

        when:
        def response = get(request)

        then:
        response.status == 200
    }
}
