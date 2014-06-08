package in.reeltime.video

import grails.test.spock.IntegrationSpec

class VideoCreationCommandIntegrationSpec extends IntegrationSpec {

    void "cannot set readonly static maxDuration"() {
        when:
        VideoCreationCommand.maxDuration = 1234

        then:
        thrown(ReadOnlyPropertyException)
    }
}
