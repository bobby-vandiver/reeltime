package in.reeltime.mail.aws

import grails.test.mixin.TestFor
import in.reeltime.mail.MailService
import spock.lang.Specification

@TestFor(SimpleEmailMailService)
class SimpleEmailMailServiceSpec extends Specification {

    void "must be an instance of MailService"() {
        expect:
        service instanceof MailService
    }
}
