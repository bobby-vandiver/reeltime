package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(AccountConfirmation)
@Mock([User])
class AccountConfirmationSpec extends Specification {

    void "user cannot be null"() {
        given:
        def accountConfirmation = new AccountConfirmation(user: null)

        expect:
        !accountConfirmation.validate(['user'])
    }

    void "user is required"() {
        given:
        def user = new User(username: 'foo', password: 'bar')
        user.springSecurityService = Stub(SpringSecurityService)
        user.save(validate: false)

        and:
        def accountConfirmation = new AccountConfirmation(user: user)

        expect:
        accountConfirmation.validate(['user'])
    }

    @Unroll
    void "code [#code] is valid [#valid]"() {
        given:
        def accountConfirmation = new AccountConfirmation(code: code)

        expect:
        accountConfirmation.validate(['code']) == valid

        where:
        code        |   valid
        null        |   false
        ''          |   false
        '1234abcde' |   true
    }

    @Unroll
    void "salt [#salt] is valid [#valid]"() {
        given:
        def accountConfirmation = new AccountConfirmation(salt: salt?.bytes)

        expect:
        accountConfirmation.validate(['salt']) == valid

        where:
        salt        |   valid
        null        |   false
        ''          |   false
        '1234abc'   |   false
        '1234abcd'  |   true
        '1234abcde' |   false
    }
}
