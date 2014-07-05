package in.reeltime.mail.aws

import in.reeltime.mail.MailService

class SimpleEmailMailService implements MailService {

    @Override
    void sendMail(String to, String from, String subject, String body) {
        // TODO: Integrate with SES
        log.debug "Entering [${this.class.name}..."
    }
}
