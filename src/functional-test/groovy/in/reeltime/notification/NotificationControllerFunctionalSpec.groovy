package in.reeltime.notification

import in.reeltime.test.rest.RestRequest
import in.reeltime.test.spec.FunctionalSpec
import spock.lang.Unroll
import static in.reeltime.test.rest.HttpHeaders.CONTENT_TYPE

class NotificationControllerFunctionalSpec extends FunctionalSpec {

    @Unroll
    void "invalid http method [#method]"() {
        given:
        def request = new RestRequest(
                url: urlFactory.notificationUrl,
                headers: [(CONTENT_TYPE): 'text/plain; charset=UTF-8']
        )

        when:
        def response = "$method"(request)

        then:
        response.status == 400
        response.body == ''

        where:
        method << ['get', 'put', 'delete']
    }
}
