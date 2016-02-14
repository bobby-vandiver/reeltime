package in.reeltime.activity

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

// Using TestFor results in a MissingMethodException for initErrors() being thrown
// by validate when executing these tests on Jenkins as unit tests. I suspect this
// is due to this class being derived from an abstract base class.
//
// ** This note is duplicated in UserReelActivityIntegrationSpec.groovy **

@Integration
@Rollback
class UserReelVideoActivityIntegrationSpec extends Specification {

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
