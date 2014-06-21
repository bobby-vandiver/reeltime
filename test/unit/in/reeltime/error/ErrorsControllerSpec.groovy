package in.reeltime.error

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ErrorsController)
class ErrorsControllerSpec extends Specification {

    void "method not allowed returns only status code"() {
        when:
        controller.methodNotAllowed()

        then:
        response.status == 405
        response.contentLength == 0
    }
}
