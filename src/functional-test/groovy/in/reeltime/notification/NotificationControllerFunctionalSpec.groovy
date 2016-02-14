package in.reeltime.notification

import in.reeltime.test.rest.RestRequest
import in.reeltime.test.spec.FunctionalSpec
import spock.lang.Unroll

class NotificationControllerFunctionalSpec extends FunctionalSpec {

    @Unroll
    void "invalid http method [#method]"() {
        given:
        def request = new RestRequest(url: urlFactory.notificationUrl)

        when:
        def response = "$method"(request)

        then:
        response.status == 400
        response.body == ''

        where:
        method << ['get', 'put', 'delete']
    }
}
