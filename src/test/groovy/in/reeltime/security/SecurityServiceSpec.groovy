package in.reeltime.security

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(SecurityService)
class SecurityServiceSpec extends Specification {

    private static TEST_LENGTH = 12
    private static TEST_CHARACTERS = 'abcdef'

    void "generate secure random secret"() {
        expect:
        service.generateSecret(TEST_LENGTH, TEST_CHARACTERS).length() == TEST_LENGTH
    }

    void "generate different passwords for subsequent executions"() {
        when:
        def firstSecret = service.generateSecret(TEST_LENGTH, TEST_CHARACTERS)
        def secondSecret = service.generateSecret(TEST_LENGTH, TEST_CHARACTERS)

        then:
        firstSecret != secondSecret
    }
}
