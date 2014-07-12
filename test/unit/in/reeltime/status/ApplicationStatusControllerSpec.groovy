package in.reeltime.status

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ApplicationStatusController)
class ApplicationStatusControllerSpec extends Specification {

    void "return 200 when available"() {
        when:
        controller.available()

        then:
        response.status == 200
        response.contentLength == 0
    }
}
