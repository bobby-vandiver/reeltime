package in.reeltime.transcoder

import in.reeltime.exceptions.TranscoderJobNotFoundException
import in.reeltime.video.Video
import static in.reeltime.transcoder.TranscoderJobStatus.*

class TranscoderJobService {

    void createJob(Video video, String jobId) {
        def job = new TranscoderJob(video: video, jobId: jobId).save()
        log.info("Created TranscoderJob [${job.id}] with jobId [$jobId] for video [${video.id}]")
    }

    TranscoderJob loadJob(String jobId) {
        log.debug "Loading transcoder job [$jobId]"
        def job = TranscoderJob.findByJobId(jobId)
        if(!job) {
            throw new TranscoderJobNotFoundException("Could not find transcoder job [$jobId]")
        }
        return job
    }

    void removeJobForVideo(Video video) {
        log.debug "Removing transcoder job for video [${video?.id}]"
        TranscoderJob.findByVideo(video)?.delete()
    }

    void complete(TranscoderJob job) {
        log.info("Transcoder job [${job.jobId}] is complete")
        job.status = Complete
        job.save()
    }
}
