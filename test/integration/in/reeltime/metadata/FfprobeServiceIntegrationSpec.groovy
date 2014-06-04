package in.reeltime.metadata

import grails.test.spock.IntegrationSpec
import spock.lang.Unroll

class FfprobeServiceIntegrationSpec extends IntegrationSpec {

    def ffprobeService

    @Unroll
    void "execute ffprobe and return JSON object representing metadata for file [#path]"() {
        given:
        def videoFile = new File(path)

        when:
        def json = ffprobeService.probeVideo(videoFile)

        then:
        json.streams[0].codec_name == 'h264'
        json.streams[0].duration == '147.188856'

        where:
        codec   |   duration        |   path
        'h264'  |   '147.188856'    |   'test/files/spidey.mp4'
    }
}
