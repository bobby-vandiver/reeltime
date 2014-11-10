package in.reeltime.transcoder

import grails.test.spock.IntegrationSpec
import in.reeltime.video.Video
import test.helper.UserFactory
import test.helper.VideoFactory

import static in.reeltime.transcoder.TranscoderJobStatus.Complete

class TranscoderJobServiceIntegrationSpec extends IntegrationSpec {

    def transcoderJobService

    Video video
    String jobId

    void setup() {
        def creator = UserFactory.createUser('creator')
        video = VideoFactory.createVideo(creator, 'transcoder-test', false)
        jobId = '1234567890123-ABCDEF'
    }

    void "create TranscoderJob and persist it"() {
        when:
        transcoderJobService.createJob(video, jobId)

        then:
        def job = TranscoderJob.findByJobId(jobId)

        and:
        job.jobId == jobId
        job.video == video
    }

    void "mark job complete"() {
        given:
        def job = new TranscoderJob(video: video, jobId: jobId).save()

        when:
        transcoderJobService.complete(job)

        then:
        def completedJob = TranscoderJob.findByJobId(jobId)
        completedJob.status == Complete
    }

    void "attempt to remove TranscoderJob for unknown video is a no-op"() {
        when:
        transcoderJobService.removeJobForVideo(video)

        then:
        notThrown(Exception)
    }

    void "remove TranscoderJob for video"() {
        given:
        new TranscoderJob(video: video, jobId: jobId).save()

        when:
        transcoderJobService.removeJobForVideo(video)

        then:
        TranscoderJob.findByJobId(jobId) == null
    }
}
