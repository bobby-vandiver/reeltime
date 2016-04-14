package in.reeltime.mail.aws

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class MXRecordMailServerExistenceServiceIntegrationSpec extends Specification {

    @Autowired
    MXRecordMailServerExistenceService MXRecordMailServerExistenceService

    @Unroll
    void "mail server [#host] exists [#exists]"() {
        expect:
        MXRecordMailServerExistenceService.exists(host) == exists

        where:
        host        |   exists
        'gmail.com' |   true
        'invalid'   |   false
    }
}
