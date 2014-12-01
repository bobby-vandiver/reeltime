package in.reeltime.transcoder

import groovy.transform.ToString
import in.reeltime.video.Video
import static in.reeltime.transcoder.TranscoderJobStatus.*

@ToString(includeNames = true)
class TranscoderJob {

    Video video
    String jobId
    TranscoderJobStatus status = Submitted

    static constraints = {
        video unique: true, nullable: false
        jobId matches: /^\d{13}-\w{6}$/, nullable: false, blank: false
    }
}
