package in.reeltime.transcoder.local

import in.reeltime.transcoder.TranscoderService
import in.reeltime.video.Video

class FfmpegTranscoderService implements TranscoderService {

    @Override
    void transcode(Video video, String output){
        log.debug("Entering ${this.class.simpleName} transcode with video [${video.id}] and output [$output]")
    }
}
