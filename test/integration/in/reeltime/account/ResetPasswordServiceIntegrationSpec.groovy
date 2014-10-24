package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.transaction.Transactional
import in.reeltime.oauth2.AccessToken
import in.reeltime.user.User
import org.springframework.transaction.annotation.Propagation
import test.helper.UserFactory
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ResetPasswordException
import test.spec.MailServiceDependentIntegrationSpec

class ResetPasswordServiceIntegrationSpec extends MailServiceDependentIntegrationSpec {

    def resetPasswordService

    def authenticationService
    def accountRemovalService

    User user
    int savedResetPasswordCodeValidityLengthInMins

    private static final String USERNAME = 'management'
    private static final String DISPLAY_NAME = 'Management Tester'
    private static final String PASSWORD = 'superSecret'
    private static final String EMAIL = 'management@test.com'

    private static final String NEW_PASSWORD = 'betterThanTheLast'

    private static final String RAW_RESET_PASSWORD_CODE = '1234abcd'
    private static final int RESET_PASSWORD_CODE_LENGTH_IN_MINS = 10

    void setup() {
        user = UserFactory.createUser(USERNAME, PASSWORD, DISPLAY_NAME, EMAIL)
        savedResetPasswordCodeValidityLengthInMins = resetPasswordService.resetPasswordCodeValidityLengthInMins
        resetPasswordService.resetPasswordCodeValidityLengthInMins = RESET_PASSWORD_CODE_LENGTH_IN_MINS
    }

    void cleanup() {
        resetPasswordService.resetPasswordCodeValidityLengthInMins = savedResetPasswordCodeValidityLengthInMins

        SpringSecurityUtils.doWithAuth(USERNAME) {
            accountRemovalService.removeAccountForCurrentUser()
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void "do not allow password reset email to be sent on an account that has not been verified"() {
        given:
        user.verified = false
        user.save()

        when:
        resetPasswordService.sendResetPasswordEmail(user, Locale.ENGLISH)

        then:
        def e = thrown(AuthorizationException)
        e.message == "Cannot reset a password if the account has not been verified"

        and:
        authenticationService.authenticateUser(USERNAME, PASSWORD)
    }

    void "send reset password email for verified account"() {
        given:
        user.verified = true
        user.save()

        when:
        resetPasswordService.sendResetPasswordEmail(user, Locale.ENGLISH)

        then:
        inMemoryMailService.sentMessages.size() == 1

        and:
        def message = inMemoryMailService.sentMessages[0]
        message.subject == 'ReelTime Password Reset'
        message.to == EMAIL
        message.from == 'noreply@reeltime.in'

        and:
        def messageRegex = /Hello (\w+), please enter the following code when prompted to reset your password: ([a-zA-z0-9]{8})/

        def matcher = (message.body =~ messageRegex)
        matcher.matches()

        and:
        matcher[0][1] == user.username
        def sentCode = matcher[0][2] as String

        def confirmationCode = AccountCode.findByUser(user)
        confirmationCode.isCodeCorrect(sentCode)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void "user has valid reset password code"() {
        given:
        def resetPasswordCodeId = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE).id

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        authenticationService.authenticateUser(USERNAME, NEW_PASSWORD)
        !authenticationService.authenticateUser(USERNAME, PASSWORD)

        and:
        !AccountCode.findById(resetPasswordCodeId)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void "user has invalid reset password code"() {
        given:
        def resetPasswordCodeId = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE).id
        def invalidCode = RAW_RESET_PASSWORD_CODE.reverse()

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, invalidCode)

        then:
        def e = thrown(ResetPasswordException)
        e.message == "The reset password code is not correct"

        and:
        !authenticationService.authenticateUser(USERNAME, NEW_PASSWORD)

        and:
        AccountCode.findById(resetPasswordCodeId)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void "user has not requested a password reset"() {
        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        def e = thrown(AuthorizationException)
        e.message == "The user has not requested a password reset"
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void "reset password code is old but has not expired"() {
        given:
        def resetPasswordCode = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE)
        def resetPasswordCodeId = resetPasswordCode.id

        and:
        ageResetPasswordCode(resetPasswordCode, RESET_PASSWORD_CODE_LENGTH_IN_MINS - 1)

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        authenticationService.authenticateUser(USERNAME, NEW_PASSWORD)

        and:
        !AccountCode.findById(resetPasswordCodeId)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void "reset password code has expired"() {
        given:
        def resetPasswordCode = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE)
        def resetPasswordCodeId = resetPasswordCode.id

        and:
        ageResetPasswordCode(resetPasswordCode, RESET_PASSWORD_CODE_LENGTH_IN_MINS)

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        def e = thrown(ResetPasswordException)
        e.message == "The reset password code has expired"

        and:
        !AccountCode.findById(resetPasswordCodeId)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void "multiple reset password code only removes the one valid"() {
        given:
        def anotherCodeId = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE.reverse()).id
        def resetPasswordCodeId = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE).id

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        authenticationService.authenticateUser(USERNAME, NEW_PASSWORD)

        and:
        !AccountCode.findById(resetPasswordCodeId)

        and:
        AccountCode.findById(anotherCodeId)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void "revoke all access tokens when password is reset"() {
        given:
        def accessToken =  new AccessToken(
                username: user.username,
                clientId: user.clients[0].clientId,
                value: 'access',
                refreshToken: 'refresh',
                tokenType: 'test',
                scope: ['scope'],
                authenticationKey: 'authKey',
                authentication: [1, 2, 3, 4] as byte[]).save()

        def accessTokenId = accessToken.id
        def resetPasswordCodeId = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE).id

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        !AccessToken.findById(accessTokenId)
        !AccountCode.findById(resetPasswordCodeId)
    }

    private static AccountCode createResetPasswordCode(User user, String rawCode) {
        def salt = 'z14aflaa'.bytes
        new AccountCode(user: user, code: rawCode, salt: salt,
                type: AccountCodeType.ResetPassword).save(flush: true)
    }

    private static void ageResetPasswordCode(AccountCode resetPassword, int numberOfMinutes) {
        Calendar calendar = Calendar.instance
        calendar.setTime(resetPassword.dateCreated)
        calendar.add(Calendar.MINUTE, -numberOfMinutes)
        resetPassword.dateCreated = calendar.time
        resetPassword.save(flush: true)
    }
}
