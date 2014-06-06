package in.reeltime.metadata

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(FfprobeService)
class FfprobeServiceSpec extends Specification {

    @Unroll
    void "throw if ffprobe cannot be found at path [#path]"() {
        given:
        service.ffprobe = path

        and:
        def file = File.createTempFile('ffprobe-not-found-test', '.mp4')

        when:
        service.probeVideo(file)

        then:
        def e = thrown(IllegalStateException)
        e.message == 'ffprobe could not be found'

        where:
        _   |   path
        _   |   null
        _   |   'path' + File.separator + 'to' + File.separator + 'nowhere'
    }
}
