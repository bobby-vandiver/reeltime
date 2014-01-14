package in.reeltime.transcoder

import in.reeltime.video.Video
import static in.reeltime.transcoder.TranscoderJobStatus.*

class TranscoderJobService {

    def createJob(Video video, String jobId) {
        def job = new TranscoderJob(video: video, jobId: jobId).save()
        log.info("Created TranscoderJob [${job.id} with jobId [$jobId] for video [${video.id}")
    }

    def complete(String jobId) {
        def job = TranscoderJob.findByJobId(jobId)
        job.status = Complete
        job.save()
    }
}
