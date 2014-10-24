package in.reeltime.mail.aws

import in.reeltime.mail.Email
import in.reeltime.mail.MailService

class SimpleEmailMailService implements MailService {

    @Override
    void sendMail(Email email) {
        // TODO: Integrate with SES
        log.debug "Entering [${this.class.name}..."

        def to = email.to
        def from = email.from
        def subject = email.subject
        def body = email.body

        // TODO: This should be modified to not log the body, which contains the confirmation code, in production!
        log.info "Sending email from [$from] to [$to] with subject [$subject] and body [$body]"
    }
}
