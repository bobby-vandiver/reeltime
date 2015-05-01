package in.reeltime.account

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll
import in.reeltime.security.CryptoService

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
        def accountCode = new AccountCode(salt: salt)

        expect:
        accountCode.validate(['salt']) == valid

        where:
        salt        |   valid
        null        |   false
        ''          |   false
        '1234abcd'  |   true
        'b' * 31    |   true
        'b' * 32    |   true
        'b' * 33    |   true
    }

    @Unroll
    void "cost [#cost] is valid [#valid]"() {
        given:
        def accountCode = new AccountCode(cost: cost)

        expect:
        accountCode.validate(['cost']) == valid

        where:
        cost        |   valid
        null        |   false
        -1          |   false
        0           |   false
        3           |   false
        4           |   true
        31          |   true
        32          |   false
    }

    void "check salt is unique"() {
        given:
        def salt = ('b' * 32)

        expect:
        AccountCode.saltIsUnique(salt)
    }

    void "check salt is not unique"() {
        given:
        def salt = ('b' * 32)

        def accountCode = new AccountCode(code: 'abcdefgh', salt: salt)
        accountCode.cryptoService = Stub(CryptoService) {
            hashBCrypt('abcdefgh', salt) >> 'hashed'
        }

        accountCode.save(validate: false)

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
