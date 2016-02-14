package in.reeltime.transcoder

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory
import in.reeltime.test.factory.VideoFactory

@Integration
@Rollback
class TranscoderJobIntegrationSpec extends Specification {

    void "video must be unique"() {
        given:
        def user = UserFactory.createTestUser()
        def video = VideoFactory.createVideo(user, 'transcoder-job')

        and:
        new TranscoderJob(video: video, jobId: '1388444889472-t01s28').save()

        when:
        def job = new TranscoderJob(video: video)

        then:
        !job.validate(['video'])
    }
}
