package in.reeltime.message

import grails.test.mixin.TestFor
import grails.validation.Validateable
import org.springframework.context.MessageSource
import spock.lang.Specification

@TestFor(LocalizedMessageService)
class LocalizedMessageServiceSpec extends Specification {

    MessageSource messageSource
    Locale locale

    void setup() {
        messageSource = Mock(MessageSource)
        service.messageSource = messageSource
        locale = Locale.ENGLISH
    }

    void "get message for code with no args"() {
        when:
        service.getMessage('foo', locale)

        then:
        1 * messageSource.getMessage('foo', [], locale)
    }

    void "get message for code with args"() {
        given:
        def args = ['bar'] as Object[]

        when:
        service.getMessage('foo', locale, args)

        then:
        1 * messageSource.getMessage('foo', args, locale)
    }

    void "get error messages for valid object"() {
        given:
        def command = new TestCommand(word: 'test')
        command.validate()

        when:
        service.getErrorMessages(command, locale)

        then:
        0 * messageSource.getMessage(*_)
    }

    void "get error messages for invalid object"() {
        given:
        def command = new TestCommand(word: null)
        command.validate()

        when:
        service.getErrorMessages(command, locale)

        then:
        1 * messageSource.getMessage(command.errors.allErrors[0], locale)
    }

    private static class TestCommand implements Validateable {
        String word

        static constraints = {
            word nullable: false
        }
    }
}
