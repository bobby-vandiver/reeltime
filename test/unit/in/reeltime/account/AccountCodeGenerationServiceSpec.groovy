package in.reeltime.account

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.security.SecurityService
import in.reeltime.user.User
import spock.lang.Specification
import in.reeltime.exceptions.AccountCodeException
import spock.lang.Unroll
import static in.reeltime.account.AccountCode.*

@TestFor(AccountCodeGenerationService)
@Mock([AccountCode])
class AccountCodeGenerationServiceSpec extends Specification {

    SecurityService securityService

    User user

    String code
    byte[] salt

    void setup() {
        user = new User(username: 'test')
        salt = ('a' * SALT_LENGTH).bytes
        code = 'abcdefgh'

        securityService = Stub(SecurityService) {
            generateSecret(CODE_LENGTH, ALLOWED_CHARACTERS) >> code
            generateSalt(SALT_LENGTH) >> salt
        }
        service.securityService = securityService
    }

    void cleanup() {
        AccountCode.metaClass = null
    }

    @Unroll
    void "salt must be unique for [#methodName] -- duplicate for first [#repeatCount] tries"() {
        given:
        int count = 0
        AccountCode.metaClass.'static'.saltIsUnique = { byte[] s ->
            return ++count >= repeatCount
        }

        expect:
        service."${methodName}"(user) == code

        where:
        methodName                          |   repeatCount
        'generateAccountConfirmationCode'   |   0
        'generateAccountConfirmationCode'   |   1
        'generateAccountConfirmationCode'   |   2
        'generateAccountConfirmationCode'   |   3
        'generateAccountConfirmationCode'   |   4
        'generateAccountConfirmationCode'   |   5

        'generateResetPasswordCode'         |   0
        'generateResetPasswordCode'         |   1
        'generateResetPasswordCode'         |   2
        'generateResetPasswordCode'         |   3
        'generateResetPasswordCode'         |   4
        'generateResetPasswordCode'         |   5
    }

    @Unroll
    void "salt must be unique for [#methodName] -- exceed max retries"() {
        given:
        AccountCode.metaClass.'static'.saltIsUnique = { byte[] s ->
            return false
        }

        when:
        service."${methodName}"(user)

        then:
        def e = thrown(AccountCodeException)
        e.message == "Failed to generate a unique salt. Exceeded max attempts"

        where:
        _   |   methodName
        _   |   'generateAccountConfirmationCode'
        _   |   'generateResetPasswordCode'
    }
}
