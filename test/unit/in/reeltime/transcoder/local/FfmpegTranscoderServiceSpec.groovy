package in.reeltime.transcoder.local

import grails.test.mixin.TestFor
import in.reeltime.transcoder.TranscoderService
import in.reeltime.storage.PathGenerationService
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
        grailsApplication.config.reeltime.transcoder.ffmpeg.path = null

        and:
        def video = new Video(masterPath: 'bar')
        def outputPath = UUID.randomUUID().toString()

        when:
        service.transcode(video, outputPath)

        then:
        def e = thrown(IllegalStateException)
        e.message == 'ffmpeg could not be found'
    }
}
