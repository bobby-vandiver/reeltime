package in.reeltime.account

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class ChangePasswordCommandSpec extends Specification {

    @Unroll
    void "new password [#password] is valid [#valid] -- same as User domain class"() {
        given:
        def command = new ChangePasswordCommand(new_password: password)

        expect:
        command.validate(['new_password']) == valid

        and:
        command.errors.getFieldError('new_password')?.code == code

        where:
        password                    |   valid   |   code
        null                        |   false   |   'nullable'
        ''                          |   false   |   'blank'
        'a'                         |   false   |   'minSize.notmet'
        'short'                     |   false   |   'minSize.notmet'
        'abcdef'                    |   true    |   null
        '!4ad#A'                    |   true    |   null
        'ABCAf1304z'                |   true    |   null
        '!#@$%^&*()-_=+][\\|<>/?'   |   true    |   null
    }
}
