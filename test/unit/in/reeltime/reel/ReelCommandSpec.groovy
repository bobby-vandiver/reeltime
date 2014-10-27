package in.reeltime.reel

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.reel.ReelCommand
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class ReelCommandSpec extends Specification {

    @Unroll
    void "reelId [#reelId] is valid [#valid]"() {
        given:
        def command = new ReelCommand(reelId: reelId)

        expect:
        command.validate(['reelId']) == valid

        and:
        command.errors.getFieldError('reelId')?.code == code

        where:
        reelId      |   valid   |   code
        null        |   false   |   'nullable'
        -1          |   true    |   null
        0           |   true    |   null
        1           |   true    |   null
    }

}
