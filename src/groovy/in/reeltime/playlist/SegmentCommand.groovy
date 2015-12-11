package in.reeltime.playlist

import grails.validation.Validateable

@Validateable(nullable = true)
class SegmentCommand {

    Integer segment_id

    static constraints = {
        segment_id nullable: false
    }
}
