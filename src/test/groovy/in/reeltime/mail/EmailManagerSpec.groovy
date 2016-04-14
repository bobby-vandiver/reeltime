package in.reeltime.mail

import spock.lang.Specification
import in.reeltime.exceptions.MailServerNotFoundException

class EmailManagerSpec extends Specification {

    EmailManager emailManager

    MailServerExistenceService mailServerExistenceService

    void setup() {
        mailServerExistenceService = Mock(MailServerExistenceService)
        emailManager = new EmailManager(mailServerExistenceService: mailServerExistenceService)
    }

    void "mail server not found"() {
        when:
        emailManager.sendMail('to', 'from', 'subject', 'body')

        then:
        1 * mailServerExistenceService.exists('to') >> false

        and:
        def e = thrown(MailServerNotFoundException)
        e.message == "Mail server [to] not found"
    }
}
