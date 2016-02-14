package in.reeltime.metadata

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class FfprobeServiceIntegrationSpec extends Specification {

    @Autowired
    FfprobeService ffprobeService

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
        'h264'  |   '147.188856'    |   'src/test/resources/files/videos/spidey.mp4'
    }
}
