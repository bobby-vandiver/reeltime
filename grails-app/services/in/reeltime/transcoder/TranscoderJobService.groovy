package in.reeltime.transcoder

import in.reeltime.video.Video
import static in.reeltime.transcoder.TranscoderJobStatus.*

class TranscoderJobService {

    void createJob(Video video, String jobId) {
        def job = new TranscoderJob(video: video, jobId: jobId).save()
        log.info("Created TranscoderJob [${job.id}] with jobId [$jobId] for video [${video.id}]")
    }

    TranscoderJob loadJob(String jobId) {
        TranscoderJob.findByJobId(jobId)
    }

    void removeJobForVideo(Video video) {
        TranscoderJob.findByVideo(video)?.delete()
    }

    void complete(TranscoderJob job) {
        log.info("Transcoder job [${job.jobId}] is complete")
        job.status = Complete
        job.save()
    }
}
