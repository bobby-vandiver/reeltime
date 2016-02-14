package in.reeltime.metadata

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(StreamMetadataService)
class StreamMetadataServiceSpec extends Specification {

    FfprobeService ffprobeService
    File videoFile

    void setup() {
        ffprobeService = Mock(FfprobeService)
        service.ffprobeService = ffprobeService
        videoFile = File.createTempFile('streams-test', '.mp4')
    }

    void "no streams in video container"() {
        when:
        def streams = service.extractStreams(videoFile)

        then:
        streams instanceof List

        and:
        streams.empty

        and:
        1 * ffprobeService.probeVideo(videoFile) >> ffprobeResult

        where:
        _   |   ffprobeResult
        _   |   null
        _   |   [:]
        _   |   [streams: []]
    }

    void "one stream in video container"() {
        given:
        def ffprobeResult = [streams: [
                [codec_name: 'h264', duration: '123.456']
        ] ]

        when:
        def streams = service.extractStreams(videoFile)

        then:
        streams.size() == 1
        streams[0].codecName == 'h264'
        streams[0].duration == '123.456'

        and:
        1 * ffprobeService.probeVideo(videoFile) >> ffprobeResult
    }

    void "multiple streams in video container"() {
        given:
        def ffprobeResult = [
            streams: [
                    [codec_name: 'h264', duration: '147.188856'],
                    [codec_name: 'aac', duration: '147.214512'],
                    [codec_name: 'mjpeg', duration: '147.215000']
            ]
        ]

        when:
        def streams = service.extractStreams(videoFile)

        then:
        streams.size() == 3

        and:
        streams[0].codecName == 'h264'
        streams[0].duration == '147.188856'

        and:
        streams[1].codecName == 'aac'
        streams[1].duration == '147.214512'

        and:
        streams[2].codecName == 'mjpeg'
        streams[2].duration == '147.215000'

        and:
        1 * ffprobeService.probeVideo(videoFile) >> ffprobeResult
    }
}
