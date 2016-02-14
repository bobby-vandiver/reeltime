package in.reeltime.reel

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class ReelCommandSpec extends Specification {

    @Unroll
    void "reel_id [#reelId] is valid [#valid]"() {
        given:
        def command = new ReelCommand(reel_id: reelId)

        expect:
        command.validate(['reel_id']) == valid

        and:
        command.errors.getFieldError('reel_id')?.code == code

        where:
        reelId      |   valid   |   code
        null        |   false   |   'nullable'
        -1          |   true    |   null
        0           |   true    |   null
        1           |   true    |   null
    }

}
