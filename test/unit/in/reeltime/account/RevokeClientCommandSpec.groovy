package in.reeltime.account

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class RevokeClientCommandSpec extends Specification {

    @Unroll
    void "client_id [#clientId] is valid [#valid]"() {
        given:
        def command = new RevokeClientCommand(client_id: clientId)

        expect:
        command.validate(['client_id']) == valid

        and:
        command.errors.getFieldError('client_id')?.code == code

        where:
        clientId    |   valid   |   code
        null        |   false   |   'nullable'
        ''          |   false   |   'blank'
        'a'         |   true    |   null
        'abcd123'   |   true    |   null
    }
}
