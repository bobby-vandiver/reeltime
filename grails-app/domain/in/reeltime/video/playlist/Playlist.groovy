package in.reeltime.video.playlist

import in.reeltime.video.Video

class Playlist {

    SortedSet segments

    static belongsTo = [video: Video]
    static hasMany = [segments: Segment]

    int getLength() {
        segments.size()
    }

    static transients = ['length']
}
