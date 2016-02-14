package in.reeltime.reel

import grails.validation.Validateable

class ReelCommand implements Validateable {

    Long reel_id

    static constraints = {
        reel_id nullable: false
    }
}
