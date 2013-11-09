package in.reeltime.video

class Segment {

    int segmentId
    String location
    String duration

    static constraints = {
        segmentId min: 0
        location blank: false, nullable: false
        duration matches: /^\d+(.\d+)?/
    }
}
