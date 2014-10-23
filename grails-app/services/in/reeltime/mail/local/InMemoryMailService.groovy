package in.reeltime.mail.local

import in.reeltime.mail.MailService
import in.reeltime.mail.Email

class InMemoryMailService implements MailService {

    List<Email> sentMessages = []

    @Override
    void sendMail(String to, String from, String subject, String body) {
        log.debug "Entering [${this.class.name}..."
        log.info "Sending email from [$from] to [$to] with subject [$subject] and body [$body]"
        sentMessages << new Email(to: to, from: from, subject: subject, body: body)
    }

    void deleteAllMessages() {
        sentMessages.removeAll { it }
    }
}
