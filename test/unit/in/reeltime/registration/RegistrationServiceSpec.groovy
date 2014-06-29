package in.reeltime.registration

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.oauth2.Client
import in.reeltime.oauth2.ClientService
import in.reeltime.user.User
import in.reeltime.user.UserService
import in.reeltime.exceptions.VerificationException
import spock.lang.Specification

@TestFor(RegistrationService)
@Mock([User, AccountVerification])
class RegistrationServiceSpec extends Specification {

    UserService userService
    ClientService clientService

    SpringSecurityService springSecurityService

    private static final String RAW_VERIFICATION_CODE = '1234abcd'

    private static final int VERIFICATION_CODE_LENGTH_IN_DAYS = 7

    void setup() {
        userService = Mock(UserService)
        clientService = Mock(ClientService)
        springSecurityService = Mock(SpringSecurityService)

        service.userService = userService
        service.clientService = clientService
        service.springSecurityService = springSecurityService

        service.verificationCodeValidityLengthInDays = VERIFICATION_CODE_LENGTH_IN_DAYS
    }

    void "return client id and client secret in registration result"() {
        given:
        def email = 'foo@test.com'
        def username = 'foo'
        def password = 'bar'
        def clientName = 'something'

        and:
        def clientId = 'buzz'
        def clientSecret = 'bazz'

        and:
        def client = new Client()

        and:
        def command = new RegistrationCommand(username: username, password: password,
                email: email, client_name: clientName)

        when:
        def result = service.registerUserAndClient(command)

        then:
        result.clientId == clientId
        result.clientSecret == clientSecret

        and:
        1 * clientService.generateClientId() >> clientId
        1 * clientService.generateClientSecret() >> clientSecret
        1 * clientService.createClient(clientName, clientId, clientSecret) >> client

        and:
        1 * userService.createUser(username, password, email, client)
    }

    void "current user has valid verification code"() {
        given:
        def user = createUser('current')
        def accountVerificationId = createAccountVerification(user, RAW_VERIFICATION_CODE).id

        when:
        service.verifyAccount(RAW_VERIFICATION_CODE)

        then:
        user.verified

        and:
        !AccountVerification.findById(accountVerificationId)

        and:
        1 * springSecurityService.currentUser >> user
        1 * userService.updateUser(user)
    }

    void "verification code is old but has not expired"() {
        given:
        def user = createUser('current')

        and:
        def accountVerification = createAccountVerification(user, RAW_VERIFICATION_CODE)
        def accountVerificationId = accountVerification.id

        and:
        ageAccountVerification(accountVerification, VERIFICATION_CODE_LENGTH_IN_DAYS - 1)

        when:
        service.verifyAccount(RAW_VERIFICATION_CODE)

        then:
        user.verified

        and:
        !AccountVerification.findById(accountVerificationId)

        and:
        1 * springSecurityService.currentUser >> user
        1 * userService.updateUser(user)
    }

    void "verification code was issued to current user but has expired"() {
        given:
        def user = createUser('current')

        and:
        def accountVerification = createAccountVerification(user, RAW_VERIFICATION_CODE)
        def accountVerificationId = accountVerification.id

        and:
        ageAccountVerification(accountVerification, VERIFICATION_CODE_LENGTH_IN_DAYS)

        when:
        service.verifyAccount(RAW_VERIFICATION_CODE)

        then:
        def e = thrown(VerificationException)
        e.message == 'The verification code for user [current] has expired'

        and:
        !user.verified

        and:
        !AccountVerification.findById(accountVerificationId)

        and:
        1 * springSecurityService.currentUser >> user
        0 * userService.updateUser(user)
    }

    void "current user has not been issued an account verification code"() {
        given:
        def user = createUser('current')

        when:
        service.verifyAccount(RAW_VERIFICATION_CODE)

        then:
        def e = thrown(VerificationException)
        e.message == 'The verification code is not associated with user [current]'

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

        def accountVerificationId = createAccountVerification(originalUser, RAW_VERIFICATION_CODE).id

        when:
        service.verifyAccount(RAW_VERIFICATION_CODE)

        then:
        def e = thrown(VerificationException)
        e.message == 'The verification code is not associated with user [current]'

        and:
        !originalUser.verified

        and:
        AccountVerification.findById(accountVerificationId)

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

    private AccountVerification createAccountVerification(User user, String rawCode) {
        def salt = 'z14aflaa'.bytes
        def hashedCode = service.hashVerificationCode(rawCode, salt)
        new AccountVerification(user: user, code: hashedCode, salt: salt).save(flush: true)
    }

    private static void ageAccountVerification(AccountVerification verification, int numberOfDays) {
        Calendar calendar = Calendar.instance
        calendar.setTime(verification.dateCreated)
        calendar.add(Calendar.DAY_OF_MONTH, -numberOfDays)
        verification.dateCreated = calendar.time
        verification.save(flush: true)
    }
}
