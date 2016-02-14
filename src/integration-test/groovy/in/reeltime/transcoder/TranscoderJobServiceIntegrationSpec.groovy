package in.reeltime.transcoder

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.exceptions.TranscoderJobNotFoundException
import in.reeltime.video.Video
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory
import in.reeltime.test.factory.VideoFactory

import static in.reeltime.transcoder.TranscoderJobStatus.Complete

@Integration
@Rollback
class TranscoderJobServiceIntegrationSpec extends Specification {

    @Autowired
    TranscoderJobService transcoderJobService

    Video video
    String jobId

    void "create TranscoderJob and persist it"() {
        given:
        setupData()

        when:
        transcoderJobService.createJob(video, jobId)

        then:
        def job = TranscoderJob.findByJobId(jobId)

        and:
        job.jobId == jobId
        job.video == video
    }

    void "attempt to load job that does not exist"() {
        given:
        setupData()

        when:
        transcoderJobService.loadJob(jobId)

        then:
        def e = thrown(TranscoderJobNotFoundException)
        e.message == "Could not find transcoder job [$jobId]"
    }

    void "mark job complete"() {
        given:
        setupData()

        and:
        def job = new TranscoderJob(video: video, jobId: jobId).save()

        when:
        transcoderJobService.complete(job)

        then:
        def completedJob = TranscoderJob.findByJobId(jobId)
        completedJob.status == Complete
    }

    void "attempt to remove TranscoderJob for unknown video is a no-op"() {
        given:
        setupData()

        when:
        transcoderJobService.removeJobForVideo(video)

        then:
        notThrown(Exception)
    }

    void "remove TranscoderJob for video"() {
        given:
        setupData()

        and:
        new TranscoderJob(video: video, jobId: jobId).save()

        when:
        transcoderJobService.removeJobForVideo(video)

        then:
        TranscoderJob.findByJobId(jobId) == null
    }

    private void setupData() {
        def creator = UserFactory.createUser('creator')
        video = VideoFactory.createVideo(creator, 'transcoder-test', false)
        jobId = '1234567890123-ABCDEF'
    }
}
