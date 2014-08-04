package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.user.User
import in.reeltime.user.UserService
import in.reeltime.exceptions.ConfirmationException
import spock.lang.Specification

@TestFor(RegistrationService)
@Mock([User, AccountConfirmation])
class RegistrationServiceSpec extends Specification {

    UserService userService
    SpringSecurityService springSecurityService

    private static final String RAW_CONFIRMATION_CODE = '1234abcd'
    private static final int CONFIRMATION_CODE_LENGTH_IN_DAYS = 7

    void setup() {
        userService = Mock(UserService)
        service.userService = userService

        springSecurityService = Mock(SpringSecurityService)
        service.springSecurityService = springSecurityService

        service.confirmationCodeValidityLengthInDays = CONFIRMATION_CODE_LENGTH_IN_DAYS
    }

    void "current user has valid confirmation code"() {
        given:
        def user = createUser('current')
        def accountConfirmationId = createAccountConfirmation(user, RAW_CONFIRMATION_CODE).id

        when:
        service.confirmAccount(RAW_CONFIRMATION_CODE)

        then:
        user.verified

        and:
        !AccountConfirmation.findById(accountConfirmationId)

        and:
        1 * springSecurityService.currentUser >> user
        1 * userService.updateUser(user)
    }

    void "confirmation code is old but has not expired"() {
        given:
        def user = createUser('current')

        and:
        def accountVerification = createAccountConfirmation(user, RAW_CONFIRMATION_CODE)
        def accountConfirmationId = accountVerification.id

        and:
        ageAccountConfirmation(accountVerification, CONFIRMATION_CODE_LENGTH_IN_DAYS - 1)

        when:
        service.confirmAccount(RAW_CONFIRMATION_CODE)

        then:
        user.verified

        and:
        !AccountConfirmation.findById(accountConfirmationId)

        and:
        1 * springSecurityService.currentUser >> user
        1 * userService.updateUser(user)
    }

    void "confirmation code was issued to current user but has expired"() {
        given:
        def user = createUser('current')

        and:
        def accountVerification = createAccountConfirmation(user, RAW_CONFIRMATION_CODE)
        def accountConfirmationId = accountVerification.id

        and:
        ageAccountConfirmation(accountVerification, CONFIRMATION_CODE_LENGTH_IN_DAYS)

        when:
        service.confirmAccount(RAW_CONFIRMATION_CODE)

        then:
        def e = thrown(ConfirmationException)
        e.message == 'The confirmation code for user [current] has expired'

        and:
        !user.verified

        and:
        !AccountConfirmation.findById(accountConfirmationId)

        and:
        1 * springSecurityService.currentUser >> user
        0 * userService.updateUser(user)
    }

    void "current user has not been issued an account confirmation code"() {
        given:
        def user = createUser('current')

        when:
        service.confirmAccount(RAW_CONFIRMATION_CODE)

        then:
        def e = thrown(ConfirmationException)
        e.message == 'The confirmation code is not associated with user [current]'

        and:
        !user.verified

        and:
        1 * springSecurityService.currentUser >> user
        0 * userService.updateUser(user)
    }

    void "current user is not associated with the presented code"() {
        given:
        def currentUser = createUser('current')
        def originalUser = createUser('original')

        def accountConfirmationId = createAccountConfirmation(originalUser, RAW_CONFIRMATION_CODE).id

        when:
        service.confirmAccount(RAW_CONFIRMATION_CODE)

        then:
        def e = thrown(ConfirmationException)
        e.message == 'The confirmation code is not associated with user [current]'

        and:
        !originalUser.verified

        and:
        AccountConfirmation.findById(accountConfirmationId)

        and:
        1 * springSecurityService.currentUser >> currentUser

        and:
        0 * userService.updateUser(currentUser)
        0 * userService.updateUser(originalUser)
    }

    private User createUser(String username) {
        def user = new User(username: username)
        user.springSecurityService = Stub(SpringSecurityService)
        user.save(validate: false)
    }

    private AccountConfirmation createAccountConfirmation(User user, String rawCode) {
        def salt = 'z14aflaa'.bytes
        def hashedCode = service.hashConfirmationCode(rawCode, salt)
        new AccountConfirmation(user: user, code: hashedCode, salt: salt).save(flush: true)
    }

    private static void ageAccountConfirmation(AccountConfirmation confirmation, int numberOfDays) {
        Calendar calendar = Calendar.instance
        calendar.setTime(confirmation.dateCreated)
        calendar.add(Calendar.DAY_OF_MONTH, -numberOfDays)
        confirmation.dateCreated = calendar.time
        confirmation.save(flush: true)
    }
}
