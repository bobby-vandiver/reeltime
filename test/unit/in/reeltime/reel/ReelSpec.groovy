package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Reel)
class ReelSpec extends Specification {

    void "a reel without an owner is impossible"() {
        given:
        def reel = new Reel()

        expect:
        !reel.validate(['owner'])
    }

    void "a reel must have an owner"() {
        given:
        def reel = new Reel(owner: new User())

        expect:
        reel.validate(['owner'])
    }

    void "a reel with no audience is impossible"() {
        given:
        def reel = new Reel()

        expect:
        !reel.validate(['audience'])
    }

    void "a reel must have one audience"() {
        given:
        def reel = new Reel(audience: new Audience())

        expect:
        reel.validate(['audience'])
    }

    @Unroll
    void "name [#name] is valid [#valid]"() {
        given:
        def reel = new Reel(name: name)

        expect:
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
