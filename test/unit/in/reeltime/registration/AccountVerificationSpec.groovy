package in.reeltime.registration

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.registration.AccountVerification
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(AccountVerification)
@Mock([User])
class AccountVerificationSpec extends Specification {

    void "user cannot be null"() {
        given:
        def accountVerification = new AccountVerification(user: null)

        expect:
        !accountVerification.validate(['user'])
    }

    void "user is required"() {
        given:
        def user = new User(username: 'foo', password: 'bar')
        user.springSecurityService = Stub(SpringSecurityService)
        user.save(validate: false)

        and:
        def accountVerification = new AccountVerification(user: user)

        expect:
        accountVerification.validate(['user'])
    }

    @Unroll
    void "code [#code] is valid [#valid]"() {
        given:
        def accountVerification = new AccountVerification(code: code)

        expect:
        accountVerification.validate(['code']) == valid

        where:
        code        |   valid
        null        |   false
        ''          |   false
        '1234abcde' |   true
    }

    @Unroll
    void "salt [#salt] is valid [#valid]"() {
        given:
        def accountVerification = new AccountVerification(salt: salt?.bytes)

        expect:
        accountVerification.validate(['salt']) == valid

        where:
        salt        |   valid
        null        |   false
        ''          |   false
        '1234abc'   |   false
        '1234abcd'  |   true
        '1234abcde' |   false
    }
}
