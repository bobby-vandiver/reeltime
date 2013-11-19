package in.reeltime.video

class Playlist {

    SortedSet segments

    static belongsTo = [video: Video]
    static hasMany = [segments: Segment]

    int getLength() {
        segments.size()
    }

    static transients = ['length']
}
