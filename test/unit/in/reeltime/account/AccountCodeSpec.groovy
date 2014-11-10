package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(AccountCode)
@Mock([User])
class AccountCodeSpec extends Specification {

    void "user cannot be null"() {
        given:
        def accountCode = new AccountCode(user: null)

        expect:
        !accountCode.validate(['user'])
    }

    void "user is required"() {
        given:
        def user = new User(username: 'foo', password: 'bar')

        and:
        def accountCode = new AccountCode(user: user)

        expect:
        accountCode.validate(['user'])
    }

    @Unroll
    void "code [#code] is valid [#valid]"() {
        given:
        def accountCode = new AccountCode(code: code)

        expect:
        accountCode.validate(['code']) == valid

        where:
        code        |   valid
        null        |   false
        ''          |   false
        '1234abcde' |   true
    }

    @Unroll
    void "salt [#salt] is valid [#valid]"() {
        given:
        def accountCode = new AccountCode(salt: salt?.bytes)

        expect:
        accountCode.validate(['salt']) == valid

        where:
        salt        |   valid
        null        |   false
        ''          |   false
        '1234abcd'  |   false
        'b' * 31    |   false
        'b' * 32    |   true
        'b' * 33    |   false
    }

    void "check salt is unique"() {
        given:
        byte[] salt = ('b' * 32).bytes

        expect:
        AccountCode.saltIsUnique(salt)
    }

    void "check salt is not unique"() {
        given:
        byte[] salt = ('b' * 32).bytes
        new AccountCode(code: 'abcdefgh', salt: salt).save(validate: false)

        expect:
        !AccountCode.saltIsUnique(salt)
    }

    @Unroll
    void "type [#type] is valid [#valid]"() {
        given:
        def accountCode = new AccountCode(type: type)

        expect:
        accountCode.validate(['type']) == valid

        where:
        type                                    |   valid
        null                                    |   false
        AccountCodeType.AccountConfirmation     |   true
        AccountCodeType.ResetPassword           |   true
    }
}
