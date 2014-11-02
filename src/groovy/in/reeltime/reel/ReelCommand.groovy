package in.reeltime.reel

import grails.validation.Validateable

@Validateable
class ReelCommand {

    Long reel_id

    static constraints = {
        reel_id nullable: false
    }
}
