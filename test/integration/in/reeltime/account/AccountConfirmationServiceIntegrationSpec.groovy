package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ConfirmationException
import test.helper.UserFactory

class AccountConfirmationServiceIntegrationSpec extends IntegrationSpec {

    def accountConfirmationService

    User user
    int savedConfirmationCodeValidityLengthInDays

    private static final String RAW_CONFIRMATION_CODE = '1234abcd'
    private static final int CONFIRMATION_CODE_LENGTH_IN_DAYS = 7

    void setup() {
        user = UserFactory.createUser('current')

        savedConfirmationCodeValidityLengthInDays = accountConfirmationService.confirmationCodeValidityLengthInDays
        accountConfirmationService.confirmationCodeValidityLengthInDays = CONFIRMATION_CODE_LENGTH_IN_DAYS
    }

    void cleanup() {
        accountConfirmationService.confirmationCodeValidityLengthInDays = savedConfirmationCodeValidityLengthInDays
    }

    void "current user has valid confirmation code"() {
        given:
        def accountConfirmationId = createAccountConfirmation(user, RAW_CONFIRMATION_CODE).id

        when:
        SpringSecurityUtils.doWithAuth(user.username) {
            accountConfirmationService.confirmAccount(RAW_CONFIRMATION_CODE)
        }

        then:
        user.verified

        and:
        !AccountCode.findById(accountConfirmationId)
    }

    void "current user does not have a valid confirmation code"() {
        given:
        def accountConfirmationId = createAccountConfirmation(user, RAW_CONFIRMATION_CODE).id

        and:
        def invalidCode = RAW_CONFIRMATION_CODE.reverse()

        when:
        SpringSecurityUtils.doWithAuth(user.username) {
            accountConfirmationService.confirmAccount(invalidCode)
        }

        then:
        def e = thrown(ConfirmationException)
        e.message == "The confirmation code is not correct"

        and:
        !user.verified

        and:
        AccountCode.findById(accountConfirmationId)
    }

    void "confirmation code is old but has not expired"() {
        given:
        def accountVerification = createAccountConfirmation(user, RAW_CONFIRMATION_CODE)
        def accountConfirmationId = accountVerification.id

        and:
        ageAccountConfirmation(accountVerification, CONFIRMATION_CODE_LENGTH_IN_DAYS - 1)

        when:
        SpringSecurityUtils.doWithAuth(user.username) {
            accountConfirmationService.confirmAccount(RAW_CONFIRMATION_CODE)
        }

        then:
        user.verified

        and:
        !AccountCode.findById(accountConfirmationId)
    }

    void "confirmation code was issued to current user but has expired"() {
        given:
        def accountVerification = createAccountConfirmation(user, RAW_CONFIRMATION_CODE)
        def accountConfirmationId = accountVerification.id

        and:
        ageAccountConfirmation(accountVerification, CONFIRMATION_CODE_LENGTH_IN_DAYS)

        when:
        SpringSecurityUtils.doWithAuth(user.username) {
            accountConfirmationService.confirmAccount(RAW_CONFIRMATION_CODE)
        }

        then:
        def e = thrown(ConfirmationException)
        e.message == 'The confirmation code for user [current] has expired'

        and:
        !user.verified

        and:
        !AccountCode.findById(accountConfirmationId)
    }

    void "current user has not been issued an account confirmation code"() {
        when:
        SpringSecurityUtils.doWithAuth(user.username) {
            accountConfirmationService.confirmAccount(RAW_CONFIRMATION_CODE)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == 'The confirmation code is not associated with user [current]'

        and:
        !user.verified
    }

    void "current user is not associated with the presented code"() {
        given:
        def originalUser = UserFactory.createUser('original')
        def accountConfirmationId = createAccountConfirmation(originalUser, RAW_CONFIRMATION_CODE).id

        when:
        SpringSecurityUtils.doWithAuth(user.username) {
            accountConfirmationService.confirmAccount(RAW_CONFIRMATION_CODE)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == 'The confirmation code is not associated with user [current]'

        and:
        !originalUser.verified

        and:
        AccountCode.findById(accountConfirmationId)
    }

    void "current user has multiple confirmation code -- only remove the valid one on confirmation"() {
        given:
        def anotherConfirmationId = createAccountConfirmation(user, RAW_CONFIRMATION_CODE.reverse()).id
        def accountConfirmationId = createAccountConfirmation(user, RAW_CONFIRMATION_CODE).id

        when:
        SpringSecurityUtils.doWithAuth(user.username) {
            accountConfirmationService.confirmAccount(RAW_CONFIRMATION_CODE)
        }

        then:
        user.verified

        and:
        !AccountCode.findById(accountConfirmationId)

        and:
        AccountCode.findById(anotherConfirmationId)
    }

    private static AccountCode createAccountConfirmation(User user, String rawCode) {
        def salt = 'z14aflaa'.bytes
        new AccountCode(user: user, code: rawCode, salt: salt,
                type: AccountCodeType.AccountConfirmation).save(flush: true)
    }

    private static void ageAccountConfirmation(AccountCode confirmation, int numberOfDays) {
        Calendar calendar = Calendar.instance
        calendar.setTime(confirmation.dateCreated)
        calendar.add(Calendar.DAY_OF_MONTH, -numberOfDays)
        confirmation.dateCreated = calendar.time
        confirmation.save(flush: true)
    }
}
