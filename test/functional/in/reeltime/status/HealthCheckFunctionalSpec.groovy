package in.reeltime.status

import helper.rest.RestRequest
import in.reeltime.FunctionalSpec

class HealthCheckFunctionalSpec extends FunctionalSpec {

    private final String HEALTHCHECK_URL = getUrlForResource('available')

    void "health check endpoint is accessible without authentication"() {
        given:
        def request = new RestRequest(url: HEALTHCHECK_URL)

        when:
        def response = get(request)

        then:
        response.status == 200
    }
}
