package in.reeltime.oauth2

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Client)
class ClientSpec extends Specification {

    @Unroll
    void "client name [#name] is valid [#valid]"() {
        given:
        def client = new Client(clientName: name)

        expect:
        client.validate(['clientName']) == valid

        where:
        name            |   valid
        null            |   false
        ''              |   false
        'some-client'   |   true
    }
}
