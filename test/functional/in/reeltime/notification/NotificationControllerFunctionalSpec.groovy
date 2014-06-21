package in.reeltime.notification

import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class NotificationControllerFunctionalSpec extends FunctionalSpec {

    String action

    @Override
    protected String getResource() {
        return "transcoder/notification/$action"
    }

    @Unroll
    void "invalid http method [#method] for action [#target]"() {
        given:
        action = target

        when:
        def response = "$method"()

        then:
        response.status == 405
        response.body == ''

        cleanup:
        action = null

        where:
        [target, method] << [['completed', 'progressing', 'warning', 'error'], ['get', 'put', 'delete']].combinations()
    }
}
