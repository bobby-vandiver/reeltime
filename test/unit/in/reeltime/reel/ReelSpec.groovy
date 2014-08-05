package in.reeltime.reel

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Reel)
class ReelSpec extends Specification {

    void "a reel with no audience is impossible"() {
        when:
        def reel = new Reel()

        then:
        !reel.validate(['audience'])
    }

    void "a reel must have one audience"() {
        given:
        def audience = new Audience()

        when:
        def reel = new Reel(audience: audience)

        then:
        reel.validate(['audience'])
    }

    @Unroll
    void "name [#name] is valid [#valid]"() {
        when:
        def reel = new Reel(name: name)

        then:
        reel.validate(['name']) == valid

        where:
        name        |   valid
        null        |   false
        ''          |   false
        'a'         |   false
        'a' * 4     |   false
        'a' * 5     |   true
        'a' * 25    |   true
        'a' * 26    |   false
    }
}
