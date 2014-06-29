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

    void "generate salt of [#size] bytes"() {
        expect:
        service.generateSalt(size).size() == size

        where:
        _   |   size
        _   |   0
        _   |   1
        _   |   5
        _   |   8
        _   |   30
    }

    void "generate different salts for subsequent requests of same size"() {
        when:
        def firstSalt = service.generateSalt(TEST_LENGTH)
        def secondSalt = service.generateSalt(TEST_LENGTH)

        then:
        !Arrays.equals(firstSalt, secondSalt)
    }
}
