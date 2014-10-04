package in.reeltime.activity

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(UserActivity)
class UserActivitySpec extends Specification {

    UserActivity activity

    void setup() {
        activity = Spy(UserActivity)
    }

    void "must have a user associated with the activity"() {
        given:
        activity.user = null

        expect:
        !activity.validate(['user'])
    }

    void "must have a type specified"() {
        given:
        activity.type = null

        expect:
        !activity.validate(['type'])
    }
}
