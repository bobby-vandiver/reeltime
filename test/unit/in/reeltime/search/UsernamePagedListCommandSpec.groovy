package in.reeltime.search

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class UsernamePagedListCommandSpec extends Specification {

    @Unroll
    void "username [#username] is valid [#valid]"() {
        given:
        def command = new UsernamePagedListCommand(username: username)

        expect:
        command.validate(['username']) == valid

        and:
        command.errors.getFieldError('username')?.code == code

        where:
        username    |   valid   |   code
        null        |   false   |   'nullable'
        ''          |   false   |   'blank'
        'a'         |   true    |   null
        'abcd123'   |   true    |   null
    }
}
