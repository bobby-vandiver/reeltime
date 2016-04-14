package in.reeltime.mail.local

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class LocalMailServerExistenceServiceIntegrationSpec extends Specification {

    @Autowired
    LocalMailServerExistenceService mailServerExistenceService

    @Unroll
    void "mail server [#host] always exists"() {
        expect:
        mailServerExistenceService.exists(host)

        where:
        _   |   host
        _   |   'gmail.com'
        _   |   'invalid'
    }
}
