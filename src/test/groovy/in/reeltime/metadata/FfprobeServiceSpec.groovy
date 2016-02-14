package in.reeltime.metadata

import grails.test.mixin.TestFor
import in.reeltime.exceptions.ProbeException
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(FfprobeService)
class FfprobeServiceSpec extends Specification {

    @Unroll
    void "unknown video file [#path]"() {
        given:
        def videoFile = path ? new File(path) : null

        when:
        service.probeVideo(videoFile)

        then:
        def e = thrown(ProbeException)

        e.cause instanceof IllegalArgumentException
        e.cause.message == "Unknown video file [${videoFile?.absolutePath}]".toString()

        where:
        _   |   path
        _   |   null
        _   |   'foo/bar/buzz'
    }

    @Unroll
    void "throw if ffprobe cannot be found at path [#path]"() {
        given:
        service.ffprobe = path

        and:
        def file = File.createTempFile('ffprobe-not-found-test', '.mp4')

        when:
        service.probeVideo(file)

        then:
        def e = thrown(ProbeException)
        e.cause.class == IllegalStateException
        e.cause.message == 'ffprobe could not be found'

        where:
        _   |   path
        _   |   null
        _   |   'path' + File.separator + 'to' + File.separator + 'nowhere'
    }
}
