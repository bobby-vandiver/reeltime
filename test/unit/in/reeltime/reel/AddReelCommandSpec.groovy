package in.reeltime.reel

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class AddReelCommandSpec extends Specification {

    @Unroll
    void "name [#name] is valid [#valid] -- inherited from Reel domain class"() {
        given:
        def command = new AddReelCommand(name: name)

        expect:
        command.validate(['name']) == valid

        and:
        command.errors.getFieldError('name')?.code == code

        where:
        name        |   valid   |   code
        null        |   false   |   'nullable'
        ''          |   false   |   'blank'
        'a'         |   false   |   'minSize.notmet'
        'a' * 4     |   false   |   'minSize.notmet'
        'a' * 5     |   true    |   null
        'a' * 25    |   true    |   null
        'a' * 26    |   false   |   'maxSize.exceeded'
    }
}
