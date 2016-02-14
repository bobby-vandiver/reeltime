package in.reeltime.error

import grails.test.mixin.TestFor
import org.apache.commons.logging.Log
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ErrorsController)
class ErrorsControllerSpec extends Specification {

    @Unroll
    void "[#method] returns only status code"() {
        when:
        controller."$method"()

        then:
        response.status == status
        response.contentLength == 0

        where:
        status  |   method
        405     |   'methodNotAllowed'
        500     |   'internalServerError'
    }

    void "do not log exception if one wasn't thrown"() {
        given:
        controller.log = Mock(Log)

        when:
        controller.internalServerError()

        then:
        0 * controller.log.error(*_)
    }

    void "log exception when handling an internal error"() {
        given:
        controller.log = Mock(Log)

        and:
        def e = new Exception('TEST')
        request.exception = e

        when:
        controller.internalServerError()

        then:
        1 * controller.log.error("Exception caught:", e)
    }
}
