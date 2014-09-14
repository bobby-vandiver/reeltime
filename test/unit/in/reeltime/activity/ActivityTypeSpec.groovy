package in.reeltime.activity

import spock.lang.Specification
import spock.lang.Unroll

class ActivityTypeSpec extends Specification {

    @Unroll
    void "[#enumValue] toString is [#toStringValue]"() {
        expect:
        ActivityType."$enumValue".toString() == toStringValue

        where:
        enumValue           |   toStringValue
        "CreateReel"        |   "create-reel"
        "AddVideoToReel"    |   "add-video-to-reel"
    }
}
