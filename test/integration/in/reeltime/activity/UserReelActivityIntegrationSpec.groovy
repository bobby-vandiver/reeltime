package in.reeltime.activity

import grails.test.spock.IntegrationSpec

// Using TestFor results in a MissingMethodException for initErrors() being thrown
// by validate when executing these tests on Jenkins as unit tests. I suspect this
// is due to this class being derived from an abstract base class.
//
// ** This note is duplicated in UserReelVideoActivityIntegrationSpec.groovy **

class UserReelActivityIntegrationSpec extends IntegrationSpec {

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
