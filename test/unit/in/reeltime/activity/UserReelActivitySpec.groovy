package in.reeltime.activity

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(UserReelActivity)
class UserReelActivitySpec extends Specification {

    @Unroll
    void "must have a reel and a user who performed the action"() {
        given:
        def activity = Spy(UserReelActivity)
        activity."$key" = value

        expect:
        activity.validate([key]) == valid

        where:
        key     |   value   |   valid
        'user'  |   null    |   false
        'reel'  |   null    |   false
    }
}
