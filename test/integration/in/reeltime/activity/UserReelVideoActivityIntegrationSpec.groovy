package in.reeltime.activity

import grails.test.spock.IntegrationSpec

// Using TestFor results in a MissingMethodException for initErrors() being thrown
// by validate when executing these tests on Jenkins as unit tests. I suspect this
// is due to this class being derived from an abstract base class.
//
// ** This note is duplicated in UserReelActivityIntegrationSpec.groovy **

class UserReelVideoActivityIntegrationSpec extends IntegrationSpec {

    void "must be an instance of UserReelActivity"() {
        given:
        def activity = new UserReelVideoActivity()

        expect:
        activity instanceof UserReelActivity
    }

    void "must have a video"() {
        given:
        def activity = new UserReelVideoActivity(video: null)

        expect:
        !activity.validate(['video'])
    }
}
