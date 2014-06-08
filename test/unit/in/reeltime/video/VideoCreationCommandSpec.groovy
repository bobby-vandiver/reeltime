package in.reeltime.video

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.metadata.StreamMetadata
import spock.lang.Specification
import spock.lang.Unroll
import test.helper.StreamMetadataListFactory

@TestMixin(GrailsUnitTestMixin)
class VideoCreationCommandSpec extends Specification {

    private static final MAX_DURATION_IN_SECONDS = 300

    void setup() {
        VideoCreationCommand.maxDuration = MAX_DURATION_IN_SECONDS
    }

    @Unroll
    void "title cannot be [#title]"() {
        given:
        def command = new VideoCreationCommand(title: title)

        expect:
        !command.validate(['title'])

        and:
        command.errors.getFieldError('title').code == code

        where:
        title   |   code
        null    |   'nullable'
        ''      |   'blank'
    }

    void "video stream cannot be null"() {
        given:
        def command = new VideoCreationCommand(videoStream: null)

        expect:
        !command.validate(['videoStream'])

        and:
        command.errors.getFieldError('videoStream').code == 'nullable'
    }

    @Unroll
    void "duration [#duration] must not exceed max duration"() {
        given:
        def videoStream = new ByteArrayInputStream('TEST'.bytes)
        def command = new VideoCreationCommand(durationInSeconds: duration, videoStream: videoStream)

        expect:
        command.validate(['durationInSeconds']) == valid

        and:
        command.errors.getFieldError('durationInSeconds')?.code == code

        where:
        duration                    |   valid   |   code
        MAX_DURATION_IN_SECONDS - 1 |   true    |   null
        MAX_DURATION_IN_SECONDS     |   true    |   null
        MAX_DURATION_IN_SECONDS + 1 |   false   |   'exceedsMaxDuration'
    }

    @Unroll
    void "video stream contains h264 stream [#h264] and aac streams [#aac] is valid [#valid]"() {
        given:
        def videoStream = new ByteArrayInputStream('TEST'.bytes)
        def command = new VideoCreationCommand(h264StreamIsPresent: h264, aacStreamIsPresent: aac, videoStream: videoStream)

        expect:
        command.validate(['h264StreamIsPresent', 'aacStreamIsPresent']) == valid

        and:
        command.errors.getFieldError('h264StreamIsPresent')?.code == h264Code
        command.errors.getFieldError('aacStreamIsPresent')?.code == aacCode

        where:
        h264    |   aac     |   valid   |   h264Code            |   aacCode
        true    |   true    |   true    |   null                |   null
        false   |   true    |   false   |   'h264IsMissing'     |   null
        true    |   false   |   false   |   null                |   'aacIsMissing'
        false   |   false   |   false   |   'h264IsMissing'     |   'aacIsMissing'
    }

    @Unroll
    void "stream metadata [#propertyName] cannot be known if video stream is null"() {
        given:
        def command = new VideoCreationCommand(("$propertyName"): value, videoStream: null)

        expect:
        !command.validate([propertyName])

        and:
        command.errors.getFieldError(propertyName)?.code == code

        where:
        propertyName            |   value   |   code
        'durationInSeconds'     |   1       |   'durationIsInvalid'
        'h264StreamIsPresent'   |   true    |   'h264IsInvalid'
        'aacStreamIsPresent'    |   true    |   'aacIsInvalid'
    }

    void "stream metadata can be null when video stream is null to avoid errors revealing internal structure"() {
        given:
        def command = new VideoCreationCommand(videoStream: null)

        expect:
        command.validate(['durationInSeconds', 'h264StreamIsPresent', 'aacStreamIsPresent'])
    }
}
