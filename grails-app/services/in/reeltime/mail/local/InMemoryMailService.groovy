package in.reeltime.mail.local

import in.reeltime.mail.MailService

class InMemoryMailService implements MailService {

    List<SentMessage> sentMessages = []

    @Override
    void sendMail(String to, String from, String subject, String body) {
        log.debug "Entering [${this.class.name}..."
        log.info "Sending email from [$from] to [$to] with subject [$subject] and body [$body]"
        sentMessages << new SentMessage(to: to, from: from, subject: subject, body: body)
    }

    void deleteAllMessages() {
        sentMessages.removeAll { it }
    }
}
