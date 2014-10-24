package in.reeltime.mail

import grails.test.spock.IntegrationSpec

class EmailManagerIntegrationSpec extends IntegrationSpec {

    def emailManager

    void "send mail adds the email to the send queue"() {
        when:
        emailManager.sendMail('src', 'dest', 'hello', 'world')

        then:
        def queue = emailManager.emails.get()
        queue.size() == 1

        def email = queue.peek()
        email.to == 'src'
        email.from == 'dest'
        email.subject == 'hello'
        email.body == 'world'
    }

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
}
