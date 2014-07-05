package in.reeltime.mail.local

import grails.test.mixin.TestFor
import in.reeltime.mail.MailService
import spock.lang.Specification

@TestFor(InMemoryMailService)
class InMemoryMailServiceSpec extends Specification {

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
        service.sendMail('foo@dest.com', 'bar@src.com', 'Hello', 'World')

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
        service.sendMail('foo@dest.com', 'bar@src.com', 'Hello', 'World')

        when:
        service.deleteAllMessages()

        then:
        service.sentMessages.empty
    }
}
