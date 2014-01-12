package in.reeltime.transcoder.local

import grails.test.mixin.TestFor
import in.reeltime.transcoder.TranscoderService
import spock.lang.Specification

@TestFor(FfmpegTranscoderService)
class FfmpegTranscoderServiceSpec extends Specification {

    void "must be an instance of TranscoderService"() {
        expect:
        service instanceof TranscoderService
    }
}
