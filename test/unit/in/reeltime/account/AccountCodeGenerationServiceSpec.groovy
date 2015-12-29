package in.reeltime.account

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.exceptions.AccountCodeException
import in.reeltime.security.CryptoService
import in.reeltime.security.SecurityService
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

import static in.reeltime.account.AccountCode.getALLOWED_CHARACTERS
import static in.reeltime.account.AccountCode.getCODE_LENGTH

@TestFor(AccountCodeGenerationService)
@Mock([AccountCode])
class AccountCodeGenerationServiceSpec extends Specification {

    SecurityService securityService
    CryptoService cryptoService

    User user

    String code
    String salt

    Integer savedCostFactor

    void setup() {
        user = new User(username: 'test')
        salt = 'salt'
        code = 'abcdefgh'

        savedCostFactor = service.costFactor
        service.costFactor = 4

        securityService = Stub(SecurityService) {
            generateSecret(CODE_LENGTH, ALLOWED_CHARACTERS) >> code
        }

        cryptoService = Stub(CryptoService) {
            generateBCryptSalt(_) >> salt
        }

        service.securityService = securityService
        service.cryptoService = cryptoService
    }

    void cleanup() {
        service.costFactor = savedCostFactor
        AccountCode.metaClass = null
    }

    @Unroll
    void "salt must be unique for [#methodName] -- duplicate for first [#repeatCount] tries"() {
        given:
        int count = 0
        AccountCode.metaClass.'static'.saltIsUnique = { String s ->
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
        AccountCode.metaClass.'static'.saltIsUnique = { String s ->
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
