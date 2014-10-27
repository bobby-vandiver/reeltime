package in.reeltime.video

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class VideoCommandSpec extends Specification {

    @Unroll
    void "videoId [#videoId] is valid [#valid]"() {
        given:
        def command = new VideoCommand(videoId: videoId)

        expect:
        command.validate(['videoId']) == valid

        and:
        command.errors.getFieldError('videoId')?.code == code

        where:
        videoId     |   valid   |   code
        null        |   false   |   'nullable'
        -1          |   true    |   null
        0           |   true    |   null
        1           |   true    |   null
    }

}
