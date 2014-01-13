package in.reeltime.transcoder

import in.reeltime.video.Video

class TranscoderJobService {

    def createJob(Video video, String jobId) {
        log.info("Creating TranscoderJob with jobId [$jobId] for video [${video.id}")
    }

    def complete(String jobId) {

    }
}
