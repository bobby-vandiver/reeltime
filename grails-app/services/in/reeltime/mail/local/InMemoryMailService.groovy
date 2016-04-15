package in.reeltime.mail.local

import grails.transaction.Transactional
import in.reeltime.mail.Email
import in.reeltime.mail.MailService

@Transactional
class InMemoryMailService implements MailService {

    List<Email> sentMessages = [].asSynchronized()

    @Override
    void sendMail(Email email) {
        log.debug "Entering [${this.class.name}..."

        def to = email.to
        def from = email.from
        def subject = email.subject
        def body = email.body

        log.info "Sending email from [$from] to [$to] with subject [$subject] and body [$body]"
        sentMessages << new Email(to: to, from: from, subject: subject, body: body)
    }

    void deleteAllMessages() {
        sentMessages.removeAll { it }
    }
}
