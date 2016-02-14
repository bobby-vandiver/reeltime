package in.reeltime.account

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class ChangeDisplayNameCommandSpec extends Specification {

    @Unroll
    void "display name [#displayName] is valid [#valid]"() {
        given:
        def command = new ChangeDisplayNameCommand(new_display_name: displayName)

        expect:
        command.validate(['new_display_name']) == valid

        and:
        command.errors.getFieldError('new_display_name')?.code == code

        where:
        displayName             |   valid   |   code
        null                    |   false   |   'nullable'
        ''                      |   false   |   'blank'
        ' '                     |   false   |   'blank'
        'a'                     |   false   |   'matches.invalid'
        ' a'                    |   false   |   'matches.invalid'
        'a '                    |   false   |   'matches.invalid'
        '!a'                    |   false   |   'matches.invalid'
        '!ab'                   |   false   |   'matches.invalid'
        'w' * 19 + '!'          |   false   |   'matches.invalid'
        'r' * 21                |   false   |   'matches.invalid'

        'xy'                    |   true    |   null
        'a b'                   |   true    |   null
        'abcde'                 |   true    |   null
        'abcdef'                |   true    |   null
        'Ab2C01faqWZ'           |   true    |   null
        '123  bbq taco'         |   true    |   null
        'r' * 20                |   true    |   null
        'a' + ' ' * 18 + 'b'    |   true    |   null
    }
}
