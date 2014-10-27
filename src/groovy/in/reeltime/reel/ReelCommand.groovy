package in.reeltime.reel

import grails.validation.Validateable

@Validateable
class ReelCommand {

    Long reelId

    static constraints = {
        reelId nullable: false
    }
}
