package in.reeltime.account

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class AccountConfirmationCommandSpec extends Specification {

    @Unroll
    void "confirmation code [#confirmationCode] is valid [#valid]"() {
        given:
        def command = new AccountConfirmationCommand(code: confirmationCode)

        expect:
        command.validate(['code']) == valid

        and:
        command.errors.getFieldError('code')?.code == code

        where:
        confirmationCode    |   valid   |   code
        null                |   false   |   'nullable'
        ''                  |   false   |   'blank'
        'a'                 |   true    |   null
        'abcd123'           |   true    |   null
    }
}
