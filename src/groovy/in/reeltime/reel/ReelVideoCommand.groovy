package in.reeltime.reel

import grails.validation.Validateable

@Validateable
class ReelVideoCommand extends ReelCommand {

    Long videoId

    static constraints = {
        videoId nullable: false
    }
}
