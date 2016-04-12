package in.reeltime.mail.aws

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.amazonaws.services.simpleemail.model.SendEmailResult
import grails.test.mixin.TestFor
import in.reeltime.aws.AwsService
import in.reeltime.mail.Email
import in.reeltime.mail.MailServerService
import in.reeltime.mail.MailService
import in.reeltime.exceptions.MailServerNotFoundException
import spock.lang.Specification

@TestFor(SimpleEmailMailService)
class SimpleEmailMailServiceSpec extends Specification {

    AmazonSimpleEmailService ses
    MailServerService mailServerService

    void setup() {
        ses = Mock(AmazonSimpleEmailService)

        service.awsService = Stub(AwsService) {
            createClient(AmazonSimpleEmailService) >> ses
        }

        mailServerService = Mock(MailServerService)
        service.mailServerService = mailServerService
    }

    void "must be an instance of MailService"() {
        expect:
        service instanceof MailService
    }

    void "mail server not found"() {
        given:
        def email = new Email(to: 'foo@bar.com')

        when:
        service.sendMail(email)

        then:
        1 * mailServerService.exists('bar.com') >> false

        and:
        def e = thrown(MailServerNotFoundException)
        e.message == "Mail server [bar.com] not found"
    }

    void "send email request is sent for email"() {
        given:
        def email = new Email(to: 'to', from: 'from', subject: 'subject', body: 'body')

        when:
        service.sendMail(email)

        then:
        1 * mailServerService.exists('to') >> true

        and:
        1 * ses.sendEmail(_) >> { SendEmailRequest request ->
            assert request.source == 'from'
            assert request.destination.toAddresses == ['to']
            assert request.message.subject.data == 'subject'
            assert request.message.body.text.data == 'body'
            return Stub(SendEmailResult)
        }
    }
}
