package in.reeltime.playlist

import grails.validation.Validateable

class SegmentCommand implements Validateable {

    Integer segment_id

    static constraints = {
        segment_id nullable: false
    }
}
