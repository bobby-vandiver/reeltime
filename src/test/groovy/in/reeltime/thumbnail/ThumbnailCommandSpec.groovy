package in.reeltime.thumbnail

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class ThumbnailCommandSpec extends Specification {

    @Unroll
    void "resolution [#resolution] is valid [#valid]"() {
        given:
        def command = new ThumbnailCommand(resolution: resolution)

        expect:
        command.validate(['resolution']) == valid

        and:
        command.errors.getFieldError('resolution')?.code == code

        where:
        resolution      |   valid   |   code
        null            |   false   |   'nullable'
        ''              |   false   |   'blank'
        'unknown'       |   false   |   'not.inList'
        'small'         |   true    |   null
        'medium'        |   true    |   null
        'large'         |   true    |   null
    }

    @Unroll
    void "resolution [#resolution] should return thumbnail resolution enum [#thumbnailResolution]"() {
        given:
        def command = new ThumbnailCommand(resolution: resolution)

        expect:
        command.thumbnailResolution == thumbnailResolution

        where:
        resolution      |   thumbnailResolution
        'unknown'       |   null
        'small'         |   ThumbnailResolution.RESOLUTION_1X
        'medium'        |   ThumbnailResolution.RESOLUTION_2X
        'large'         |   ThumbnailResolution.RESOLUTION_3X
    }

    void "invalid resolution should only report param errors"() {
        given:
        def command = new ThumbnailCommand(resolution: 'invalid')

        when:
        command.validate()

        then:
        command.errors.errorCount == 1
        command.errors.getFieldError('resolution') != null
    }
}
