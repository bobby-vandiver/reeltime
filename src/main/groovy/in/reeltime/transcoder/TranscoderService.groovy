package in.reeltime.transcoder

import in.reeltime.video.Video

interface TranscoderService {

    /**
     *  Transcodes the video and writes the segments and playlists to the specified output path.
     *
     * @param video The video to transcode. The master file has been stored and the object has been persisted prior to this call.
     * @param output The base path in the configured storage system. This must *not* end in '/'.
     */
    void transcode(Video video, String output)
}
