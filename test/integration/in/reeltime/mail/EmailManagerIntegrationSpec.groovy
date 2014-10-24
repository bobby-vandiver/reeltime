package in.reeltime.mail

import org.springframework.transaction.support.DefaultTransactionDefinition
import test.spec.MailServiceDependentIntegrationSpec

class EmailManagerIntegrationSpec extends MailServiceDependentIntegrationSpec {

    def emailManager

    def transactionManager

    void "each thread has its own queue"() {
        given:
        def t1 = new EmailThread(emailManager: emailManager, prefix: 't1')
        def t2 = new EmailThread(emailManager: emailManager, prefix: 't2')

        when:
        t1.start()
        t1.join()

        and:
        t2.start()
        t2.join()

        then:
        t1.queue.size() == 1

        and:
        def e1 = t1.queue.peek()
        e1.to == 't1-to'
        e1.from == 't1-from'
        e1.subject == 't1-subject'
        e1.body == 't1-body'

        and:
        t2.queue.size() == 1

        and:
        def e2 = t2.queue.peek()
        e2.to == 't2-to'
        e2.from == 't2-from'
        e2.subject == 't2-subject'
        e2.body == 't2-body'
    }

    void "send queued emails on transaction commit"() {
        given:
        def definition = new DefaultTransactionDefinition()
        def status = transactionManager.getTransaction(definition)

        when:
        emailManager.sendMail('e1-to', 'e1-from', 'e1-subject', 'e1-body')
        emailManager.sendMail('e2-to', 'e2-from', 'e2-subject', 'e2-body')

        and:
        transactionManager.commit(status)

        then:
        inMemoryMailService.sentMessages.size() == 2

        and:
        def e1 = inMemoryMailService.sentMessages[0]
        e1.to == 'e1-to'
        e1.from == 'e1-from'
        e1.subject == 'e1-subject'
        e1.body == 'e1-body'

        and:
        def e2 = inMemoryMailService.sentMessages[1]
        e2.to == 'e2-to'
        e2.from == 'e2-from'
        e2.subject == 'e2-subject'
        e2.body == 'e2-body'
    }

    void "no emails are sent on transaction rollback"() {
        given:
        def definition = new DefaultTransactionDefinition()
        def status = transactionManager.getTransaction(definition)

        when:
        emailManager.sendMail('e1-to', 'e1-from', 'e1-subject', 'e1-body')
        emailManager.sendMail('e2-to', 'e2-from', 'e2-subject', 'e2-body')

        and:
        transactionManager.rollback(status)

        then:
        inMemoryMailService.sentMessages.size() == 0
    }
}
