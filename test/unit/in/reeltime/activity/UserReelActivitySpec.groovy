package in.reeltime.activity

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(UserReelActivity)
class UserReelActivitySpec extends Specification {

    UserReelActivity activity

    void setup() {
        activity = new UserReelActivity()
    }

    void "must be a user activity"() {
        expect:
        activity instanceof UserActivity
    }

    void "must have a reel associated with the activity"() {
        given:
        activity.reel = null

        expect:
        !activity.validate(['reel'])
    }
}
