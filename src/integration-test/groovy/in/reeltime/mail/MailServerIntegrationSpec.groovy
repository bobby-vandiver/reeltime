package in.reeltime.mail

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class MailServerIntegrationSpec extends Specification {

    @Autowired
    MailServerService mailServerService

    @Unroll
    void "mail server [#host] exists [#exists]"() {
        expect:
        mailServerService.exists(host) == exists

        where:
        host        |   exists
        'gmail.com' |   true
        'invalid'   |   false
    }
}
