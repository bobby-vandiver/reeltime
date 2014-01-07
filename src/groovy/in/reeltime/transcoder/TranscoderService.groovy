package in.reeltime.transcoder

import in.reeltime.video.Video

interface TranscoderService {

    void transcode(Video video)
}
