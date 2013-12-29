package in.reeltime.hls.playlist

class MediaPlaylist {

    int targetDuration
    int mediaSequence

    int version
    boolean allowCache

    List<MediaSegment> segments = []
}
