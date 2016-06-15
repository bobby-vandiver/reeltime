package in.reeltime.mail.mailgun

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.mail.Email
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
class MailgunServiceIntegrationSpec extends Specification {

    private static final String DEVELOPER_EMAIL_ADDRESS = System.getProperty("DEVELOPER_EMAIL_ADDRESS")

    @Autowired
    MailgunService mailgunService

    @Ignore("This is provided as a convenience for the developer to test Mailgun integration")
    void "send email"() {
        given:
        Email email = new Email(
                to: DEVELOPER_EMAIL_ADDRESS,
                from: "integration-test@reeltime.in",
                subject: "MailgunServiceIntegrationSpec",
                body: "Sending email via Mailgun works!"
        )

        when:
        mailgunService.sendMail(email)

        then:
        noExceptionThrown()
    }
}
