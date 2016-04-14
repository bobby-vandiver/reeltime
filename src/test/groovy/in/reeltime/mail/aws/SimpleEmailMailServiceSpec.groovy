package in.reeltime.mail.aws

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.amazonaws.services.simpleemail.model.SendEmailResult
import grails.test.mixin.TestFor
import in.reeltime.aws.AwsService
import in.reeltime.mail.Email
import in.reeltime.mail.MailService
import spock.lang.Specification

@TestFor(SimpleEmailMailService)
class SimpleEmailMailServiceSpec extends Specification {

    AmazonSimpleEmailService ses

    void setup() {
        ses = Mock(AmazonSimpleEmailService)

        service.awsService = Stub(AwsService) {
            createClient(AmazonSimpleEmailService) >> ses
        }
    }

    void "must be an instance of MailService"() {
        expect:
        service instanceof MailService
    }

    void "send email request is sent for email"() {
        given:
        def email = new Email(to: 'to', from: 'from', subject: 'subject', body: 'body')

        when:
        service.sendMail(email)

        then:
        1 * ses.sendEmail(_) >> { SendEmailRequest request ->
            assert request.source == 'from'
            assert request.destination.toAddresses == ['to']
            assert request.message.subject.data == 'subject'
            assert request.message.body.text.data == 'body'
            return Stub(SendEmailResult)
        }
    }
}
