package in.reeltime.mail.aws

import in.reeltime.mail.MailService

class SimpleEmailMailService implements MailService {

    @Override
    void sendMail(String to, String from, String subject, String body) {
        // TODO: Integrate with SES
        log.debug "Entering [${this.class.name}..."

        // TODO: This should be modified to not log the body, which contains the confirmation code, in production!
        log.info "Sending email from [$from] to [$to] with subject [$subject] and body [$body]"
    }
}
