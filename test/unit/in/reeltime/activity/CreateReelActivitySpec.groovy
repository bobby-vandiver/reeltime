package in.reeltime.activity

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(CreateReelActivity)
class CreateReelActivitySpec extends Specification {

    void "must be an instance of UserReelActivity"() {
        given:
        def activity = new CreateReelActivity()

        expect:
        activity instanceof UserReelActivity
    }
}
