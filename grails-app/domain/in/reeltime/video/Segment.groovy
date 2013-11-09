package in.reeltime.video

class Segment implements Comparable {

    int segmentId
    String location
    String duration

    static constraints = {
        segmentId min: 0
        location blank: false, nullable: false
        duration matches: /^\d+(.\d+)?/
    }

    @Override
    int compareTo(obj) {
        segmentId <=> obj.segmentId
    }
}
