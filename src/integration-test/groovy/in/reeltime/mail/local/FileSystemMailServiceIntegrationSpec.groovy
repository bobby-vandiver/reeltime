package in.reeltime.mail.local

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.mail.Email
import in.reeltime.mail.MailService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class FileSystemMailServiceIntegrationSpec extends Specification {

    @Autowired
    FileSystemMailService fileSystemMailService

    Email email

    void setup() {
        email = new Email(to: 'foo@dest.com', from: 'bar@src.com', subject: 'Hello', body: 'World')
    }

    void "must be an instance of MailService"() {
        expect:
        fileSystemMailService instanceof MailService
    }

    void "create and add file to sent messages"() {
        when:
        fileSystemMailService.sendMail(email)

        then:
        fileSystemMailService.getMailBody('foo@dest.com', 'bar@src.com', 'Hello') == 'World'

        when:
        email.body = 'Space'
        fileSystemMailService.sendMail(email)

        then:
        fileSystemMailService.getMailBody('foo@dest.com', 'bar@src.com', 'Hello') == 'Space'
    }
}
