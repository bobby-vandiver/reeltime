package in.reeltime.security

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(SecretService)
class SecretServiceSpec extends Specification {

    private static REQUIRED_LENGTH = 12
    private static ALLOWED_CHARACTERS = 'abcdef'

    void "generate secure random secret"() {
        expect:
        service.generateSecret(REQUIRED_LENGTH, ALLOWED_CHARACTERS).length() == REQUIRED_LENGTH
    }

    void "generate different passwords for subsequent executions"() {
        when:
        def firstSecret = service.generateSecret(REQUIRED_LENGTH, ALLOWED_CHARACTERS)
        def secondSecret = service.generateSecret(REQUIRED_LENGTH, ALLOWED_CHARACTERS)

        then:
        firstSecret != secondSecret
    }
}
