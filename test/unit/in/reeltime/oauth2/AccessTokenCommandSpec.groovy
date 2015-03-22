package in.reeltime.oauth2

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class AccessTokenCommandSpec extends Specification {

    @Unroll
    void "access_token [#token] is valid [#valid]"() {
        given:
        def command = new AccessTokenCommand(access_token: token)

        expect:
        command.validate(['access_token']) == valid

        and:
        command.errors.getFieldError('access_token')?.code == code

        where:
        token   |   valid   |   code
        null    |   false   |   'nullable'
        ''      |   false   |   'blank'
        'a'     |   true    |   null
        'abcd'  |   true    |   null
        '12afz' |   true    |   null
        '$asf1' |   true    |   null
    }
}
