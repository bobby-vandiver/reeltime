package in.reeltime.transcoder.local

import grails.test.mixin.TestFor
import in.reeltime.transcoder.TranscoderService
import in.reeltime.exceptions.TranscoderException
import in.reeltime.video.Video
import spock.lang.Specification

@TestFor(FfmpegTranscoderService)
class FfmpegTranscoderServiceSpec extends Specification {

    void "must be an instance of TranscoderService"() {
        expect:
        service instanceof TranscoderService
    }

    void "throw if ffmpeg cannot be found"() {
        given:
        service.ffmpeg = null

        and:
        def video = new Video(masterPath: 'bar')
        def outputPath = UUID.randomUUID().toString()

        when:
        service.transcode(video, outputPath)

        then:
        def e = thrown(TranscoderException)
        e.cause.class == IllegalStateException
        e.cause.message == 'ffmpeg could not be found'
    }
}
