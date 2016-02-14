package in.reeltime.account

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ResetPasswordException
import in.reeltime.oauth2.AccessToken
import in.reeltime.security.AuthenticationService
import in.reeltime.security.CryptoService
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory

@Integration
@Rollback
class ResetPasswordServiceIntegrationSpec extends Specification {

    @Autowired
    ResetPasswordService resetPasswordService

    @Autowired
    AuthenticationService authenticationService

    @Autowired
    AccountRemovalService accountRemovalService

    @Autowired
    CryptoService cryptoService

    User user
    int resetPasswordCodeValidityLengthInMins

    private static final String USERNAME = 'management'
    private static final String DISPLAY_NAME = 'Management Tester'
    private static final String PASSWORD = 'superSecret'
    private static final String EMAIL = 'management@test.com'

    private static final String NEW_PASSWORD = 'betterThanTheLast'
    private static final String RAW_RESET_PASSWORD_CODE = '1234abcd'

    void setup() {
        resetPasswordCodeValidityLengthInMins = resetPasswordService.resetPasswordCodeValidityLengthInMins
    }

    void "do not allow password reset email to be sent on an account that has not been verified"() {
        given:
        createUser(verified: false)

        when:
        resetPasswordService.sendResetPasswordEmail(user, Locale.ENGLISH)

        then:
        def e = thrown(AuthorizationException)
        e.message == "Cannot reset a password if the account has not been verified"

        and:
        authenticationService.authenticateUser(USERNAME, PASSWORD)

        cleanup:
        removeUser()
    }

    void "user has valid reset password code"() {
        given:
        createUser()

        and:
        def resetPasswordCodeId = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE).id

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        authenticationService.authenticateUser(USERNAME, NEW_PASSWORD)
        !authenticationService.authenticateUser(USERNAME, PASSWORD)

        and:
        !AccountCode.findById(resetPasswordCodeId)

        cleanup:
        removeUser()
    }

    void "user has invalid reset password code"() {
        given:
        createUser()

        and:
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

        cleanup:
        removeUser()
    }

    void "user has not requested a password reset"() {
        given:
        createUser()

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        def e = thrown(ResetPasswordException)
        e.message == "The user has not requested a password reset"

        cleanup:
        removeUser()
    }

    void "reset password code is old but has not expired"() {
        given:
        createUser()

        and:
        def resetPasswordCode = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE)
        def resetPasswordCodeId = resetPasswordCode.id

        and:
        ageResetPasswordCode(resetPasswordCode, resetPasswordCodeValidityLengthInMins - 1)

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        authenticationService.authenticateUser(USERNAME, NEW_PASSWORD)

        and:
        !AccountCode.findById(resetPasswordCodeId)

        cleanup:
        removeUser()
    }

    void "reset password code has expired"() {
        given:
        createUser()

        and:
        def resetPasswordCode = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE)
        def resetPasswordCodeId = resetPasswordCode.id

        and:
        ageResetPasswordCode(resetPasswordCode, resetPasswordCodeValidityLengthInMins)

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        def e = thrown(ResetPasswordException)
        e.message == "The reset password code has expired"

        and:
        !AccountCode.findById(resetPasswordCodeId)

        cleanup:
        removeUser()
    }

    void "multiple reset password code only removes the one valid"() {
        given:
        createUser()

        and:
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

        cleanup:
        removeUser()
    }

    void "revoke all access tokens when password is reset"() {
        given:
        createUser()

        and:
        def accessToken =  new AccessToken(
                username: user.username,
                clientId: user.clients[0].clientId,
                value: 'access',
                refreshToken: 'refresh',
                tokenType: 'test',
                scope: ['scope'],
                expiration: new Date(),
                authenticationKey: 'authKey',
                authentication: [1, 2, 3, 4] as byte[]).save()

        def accessTokenId = accessToken.id
        def resetPasswordCodeId = createResetPasswordCode(user, RAW_RESET_PASSWORD_CODE).id

        when:
        resetPasswordService.resetPassword(USERNAME, NEW_PASSWORD, RAW_RESET_PASSWORD_CODE)

        then:
        !AccessToken.findById(accessTokenId)
        !AccountCode.findById(resetPasswordCodeId)

        cleanup:
        removeUser()
    }

    private void createUser(Map overrides = [:]) {
        user = UserFactory.createUser(USERNAME, PASSWORD, DISPLAY_NAME, EMAIL)

        overrides.each { key, value ->
            user."$key" = value
        }
        user.save()
    }

    private void removeUser() {
        SpringSecurityUtils.doWithAuth(USERNAME) {
            accountRemovalService.removeAccountForCurrentUser()
        }
    }

    private AccountCode createResetPasswordCode(User user, String rawCode) {
        def salt = cryptoService.generateBCryptSalt(10)

        new AccountCode(user: user, code: rawCode, salt: salt, cost: 10,
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
