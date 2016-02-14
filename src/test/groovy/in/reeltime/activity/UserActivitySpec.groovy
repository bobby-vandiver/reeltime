package in.reeltime.activity

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

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

    @Unroll
    void "type [#type] maps to an ActivityType"() {
        given:
        activity.type = type

        expect:
        activity.validate(['type'])

        where:
        type << ActivityType.values()*.value
    }

    @Unroll
    void "type [#type] does not map to an ActivityType"() {
        given:
        activity.type = type

        expect:
        !activity.validate(['type'])

        where:
        _   |   type
        _   |   1
        _   |   25
        _   |   75
        _   |   51
        _   |   99
    }
}
