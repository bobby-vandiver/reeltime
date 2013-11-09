package in.reeltime.video

class Segment {

    int segmentId
    String location
    double duration

    static constraints = {
        segmentId min: 0
        location blank: false, nullable: false
        duration validator: {val -> val >= 0.0 }
    }
}
