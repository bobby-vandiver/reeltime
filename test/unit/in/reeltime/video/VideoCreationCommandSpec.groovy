package in.reeltime.video

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.reel.Reel
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class VideoCreationCommandSpec extends Specification {

    private static final MAX_DURATION_IN_SECONDS = 300

    void setup() {
        VideoCreationCommand.maxDuration = MAX_DURATION_IN_SECONDS
    }

    void "creator cannot be null"() {
        given:
        def command = new VideoCreationCommand(creator: null)

        expect:
        !command.validate(['creator'])
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

    @Unroll
    void "reel name cannot be [#name]"() {
        given:
        def command = new VideoCreationCommand(reel: name)

        expect:
        !command.validate(['reel'])

        and:
        command.errors.getFieldError('reel').code == code

        where:
        name   |   code
        null    |   'nullable'
        ''      |   'blank'
    }

    void "creator does not have the reel specified"() {
        given:
        def command = new VideoCreationCommand(creator: new User(), reel: 'some reel')

        expect:
        !command.validate(['reel'])

        and:
        command.errors.getFieldError('reel').code == 'unknown'
    }

    void "creator does have the reel"() {
        given:
        def reel = new Reel(name: 'test')
        def creator = new User(reels: [reel])

        def command = new VideoCreationCommand(creator: creator, reel: 'test')

        expect:
        command.validate(['reel'])
    }

    void "skip reel validation if the creator is null"() {
        given:
        def command = new VideoCreationCommand(reel: 'some reel')

        expect:
        command.validate(['reel'])

        and:
        !command.validate(['creator'])
    }

    void "thumbnail stream cannot be null"() {
        def command = new VideoCreationCommand(thumbnailStream: null)

        expect:
        !command.validate(['thumbnailStream'])

        and:
        command.errors.getFieldError('thumbnailStream').code == 'nullable'
    }

    @Unroll
    void "video stream size flag [#validSize] is valid [#valid]"() {
        given:
        def videoStream = new ByteArrayInputStream('TEST'.bytes)
        def command = new VideoCreationCommand(videoStreamSizeIsValid: validSize, videoStream: videoStream)

        expect:
        command.validate(['videoStreamSizeIsValid']) == valid

        and:
        command.errors.getFieldError('videoStreamSizeIsValid')?.code == code

        where:
        validSize   |   valid   |   code
        null        |   false   |   'exceedsMax'
        false       |   false   |   'exceedsMax'
        true        |   true    |   null
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
        MAX_DURATION_IN_SECONDS + 1 |   false   |   'exceedsMax'
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
        h264    |   aac     |   valid   |   h264Code    |   aacCode
        true    |   true    |   true    |   null        |   null
        false   |   true    |   false   |   'missing'   |   null
        true    |   false   |   false   |   null        |   'missing'
        false   |   false   |   false   |   'missing'   |   'missing'
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
        'durationInSeconds'     |   1       |   'invalid'
        'h264StreamIsPresent'   |   true    |   'invalid'
        'aacStreamIsPresent'    |   true    |   'invalid'
        'videoStreamSizeIsValid'|   true    |   'invalid'
    }

    void "stream metadata can be null when video stream is null to avoid errors revealing internal structure"() {
        given:
        def command = new VideoCreationCommand(videoStream: null)

        expect:
        command.validate(['durationInSeconds', 'h264StreamIsPresent', 'aacStreamIsPresent'])
    }
}
