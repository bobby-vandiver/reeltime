package in.reeltime.mail.aws

import in.reeltime.mail.EmailUtils
import spock.lang.Specification
import spock.lang.Unroll

class EmailUtilsSpec extends Specification {

    @Unroll
    void "get host from email address [#email]"() {
        expect:
        EmailUtils.getHostFromEmailAddress(email) == host

        where:
        email       |   host
        null        |   null
        ''          |   ''
        'a'         |   'a'
        'a@'        |   ''
        'a@b.com'   |   'b.com'
        'a@b@c@d'   |   'b@c@d'
    }
}
