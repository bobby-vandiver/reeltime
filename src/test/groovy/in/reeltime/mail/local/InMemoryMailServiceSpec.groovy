package in.reeltime.mail.local

import grails.test.mixin.TestFor
import in.reeltime.mail.Email
import in.reeltime.mail.MailService
import spock.lang.Specification

@TestFor(InMemoryMailService)
class InMemoryMailServiceSpec extends Specification {

    Email email

    void setup() {
        email = new Email(to: 'foo@dest.com', from: 'bar@src.com', subject: 'Hello', body: 'World')
    }

    void "must be an instance of MailService"() {
        expect:
        service instanceof MailService
    }

    void "sent mail store is initially empty"() {
        expect:
        service.sentMessages.empty
    }

    void "create and add mail to in-memory store"() {
        when:
        service.sendMail(email)

        then:
        service.sentMessages.size() == 1

        and:
        def sentMail = service.sentMessages[0]
        sentMail.to == 'foo@dest.com'
        sentMail.from == 'bar@src.com'
        sentMail.subject == 'Hello'
        sentMail.body == 'World'
    }

    void "delete in-memory store"() {
        given:
        service.sendMail(email)

        when:
        service.deleteAllMessages()

        then:
        service.sentMessages.empty
    }
}
