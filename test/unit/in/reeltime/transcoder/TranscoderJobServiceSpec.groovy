package in.reeltime.transcoder

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.video.Video
import spock.lang.Specification

import static in.reeltime.transcoder.TranscoderJobStatus.*

@TestFor(TranscoderJobService)
@Mock([TranscoderJob])
class TranscoderJobServiceSpec extends Specification {

    void "create TranscoderJob and persist it"() {
        given:
        def video = new Video()
        def jobId = '1234567890123-ABCDEF'

        when:
        service.createJob(video, jobId)

        then:
        def job = TranscoderJob.findByJobId(jobId)

        and:
        job.jobId == jobId
        job.video == video
    }

    void "mark job complete"() {
        given:
        def video = new Video()
        def jobId = '1234567890123-ABCDEF'

        and:
        new TranscoderJob(video: video, jobId: jobId).save()

        when:
        service.complete(jobId)

        then:
        def job = TranscoderJob.findByJobId(jobId)
        job.status == Complete
    }

}
