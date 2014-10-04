package in.reeltime.activity

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(UserReelVideoActivity)
class UserReelVideoActivitySpec extends Specification {

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
