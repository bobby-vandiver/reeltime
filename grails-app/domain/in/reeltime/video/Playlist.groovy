package in.reeltime.video

class Playlist {

    SortedSet segments

    static hasMany = [segments: Segment]

    int getLength() {
        segments.size()
    }

    static transients = ['length']
}
