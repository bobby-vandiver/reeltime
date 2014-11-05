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
        command.reelAuthorizationService = Stub(ReelAuthorizationService) {
            reelNameIsReserved(_) >> false
        }

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

    @Unroll
    void "reserved name is not valid"() {
        def command = new AddReelCommand(name: 'uncategorized')
        command.reelAuthorizationService = Stub(ReelAuthorizationService) {
            reelNameIsReserved(_) >> true
        }

        expect:
        !command.validate(['name'])

        and:
        command.errors.getFieldError('name')?.code == 'reserved'
    }

}
