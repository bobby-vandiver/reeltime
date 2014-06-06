package in.reeltime.video

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import in.reeltime.metadata.StreamMetadata
import spock.lang.Specification
import spock.lang.Unroll
import test.helper.StreamMetadataListFactory

@TestMixin(GrailsUnitTestMixin)
class VideoCreationCommandSpec extends Specification {

    private static final MAX_DURATION_IN_SECONDS = '300'
    private static final VALID_DURATION = '123.456'

    void setup() {
        grailsApplication.config.reeltime.metadata.maxDurationInSeconds = MAX_DURATION_IN_SECONDS
    }

    void "video stream cannot be null"() {
        given:
        def command = new VideoCreationCommand(videoStream: null)

        expect:
        !command.validate(['videoStream'])
    }

    @Unroll
    void "do not allow videos that contains [#count] invalid streams"() {
        given:
        def command = new VideoCreationCommand(streams: createListOfInvalidStreams(count))

        expect:
        !command.validate(['streams'])

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
    }

    @Unroll
    void "video stream must contain h264 and aac streams"() {
        given:
        def command = new VideoCreationCommand(streams: StreamMetadataListFactory.createRequiredStreams())

        expect:
        command.validate(['streams'])
    }

    @Unroll
    void "ignore [#count] invalid streams if h264 and aac streams are present"() {
        given:
        def requiredStreams = StreamMetadataListFactory.createRequiredStreams()
        def invalidStreams = createListOfInvalidStreams(count, [duration: VALID_DURATION])

        and:
        def command = new VideoCreationCommand(streams: requiredStreams + invalidStreams)

        expect:
        command.validate(['streams'])

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   3
    }

    @Unroll
    void "include only one required codec stream [#codec] and [#invalidCount] invalid codec streams"() {
        given:
        def required = new StreamMetadata(codecName: codec, duration: VALID_DURATION)
        def invalid = createListOfInvalidStreams(invalidCount, [duration: VALID_DURATION])

        and:
        def streams = [required] + invalid
        def command = new VideoCreationCommand(streams: streams)

        expect:
        !command.validate(['streams'])

        where:
        [codec, invalidCount] << [['h264', 'aac'], [0, 1, 2, 3, 4]].combinations()
    }

    private static List<StreamMetadata> createListOfInvalidStreams(int count, Map overrides = [:]) {
        def codecName = overrides?.codecName ?: 'invalidCodec'
        def duration = overrides?.duration ?: 'invalidDuration'

        def list = []
        for(int i = 0; i < count; i++) {
            new StreamMetadata(codecName: codecName, duration: duration)
        }
        return list
    }
}
