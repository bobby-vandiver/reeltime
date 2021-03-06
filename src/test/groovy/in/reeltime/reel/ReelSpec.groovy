package in.reeltime.reel

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Reel)
class ReelSpec extends Specification {

    @Unroll
    void "name [#name] is uncategorized [#uncategorized]"() {
        given:
        def reel = new Reel(name: name)

        expect:
        reel.isUncategorizedReel() == uncategorized

        where:
        name            |   uncategorized
        null            |   false
        'something'     |   false
        'Uncategorized' |   true
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
