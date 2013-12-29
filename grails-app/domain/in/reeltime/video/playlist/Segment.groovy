package in.reeltime.video.playlist

class Segment implements Comparable {

    int segmentId
    String location
    String duration

    static belongsTo = [playlist: Playlist]

    static constraints = {
        segmentId min: 0
        location blank: false, nullable: false
        duration blank: false, nullable: false,  matches: /^\d+(.\d+)?/
    }

    @Override
    int compareTo(obj) {
        segmentId <=> obj.segmentId
    }
}
