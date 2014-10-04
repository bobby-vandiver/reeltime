package in.reeltime.activity

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(AddVideoToReelActivity)
class AddVideoToReelActivitySpec extends Specification {

    void "must be an instance of UserReelActivity"() {
        given:
        def activity = new AddVideoToReelActivity()

        expect:
        activity instanceof UserReelActivity
    }

    void "must have a video"() {
        given:
        def activity = new AddVideoToReelActivity(video: null)

        expect:
        !activity.validate(['video'])
    }
}
