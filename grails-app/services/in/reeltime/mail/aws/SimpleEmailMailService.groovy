package in.reeltime.mail.aws

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import grails.transaction.Transactional
import in.reeltime.mail.Email
import in.reeltime.mail.MailService

@Transactional
class SimpleEmailMailService implements MailService {

    def awsService

    @Override
    void sendMail(Email email) {
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
}
