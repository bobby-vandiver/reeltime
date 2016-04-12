package in.reeltime.mail.aws

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import in.reeltime.exceptions.MailServerNotFoundException
import in.reeltime.mail.Email
import in.reeltime.mail.EmailUtils
import in.reeltime.mail.MailService

class SimpleEmailMailService implements MailService {

    def awsService
    def mailServerService

    @Override
    void sendMail(Email email) {
        verifyMailServerExistsForEmailAddress(email.to)

        def destination = new Destination([email.to])
        def subjectContent = new Content(email.subject)

        def bodyContent = new Content(email.body)
        def body = new Body(bodyContent)

        def message = new Message(subjectContent, body)

        def ses = awsService.createClient(AmazonSimpleEmailService) as AmazonSimpleEmailService

        log.info "Sending email to [${email.to}] with subject [${email.subject}]"

        def request = new SendEmailRequest(email.from, destination, message)
        def result = ses.sendEmail(request)

        log.debug "Email sent to [${email.to}] with subject [${email.subject}] -- messageId [${result.messageId}]"
    }

    private void verifyMailServerExistsForEmailAddress(String emailAddress) {
        def host = EmailUtils.getHostFromEmailAddress(emailAddress)

        if (!mailServerService.exists(host)) {
            throw new MailServerNotFoundException("Mail server [$host] not found")
        }
    }
}
