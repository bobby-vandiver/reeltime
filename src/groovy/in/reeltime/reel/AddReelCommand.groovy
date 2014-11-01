package in.reeltime.reel

import grails.validation.Validateable

@Validateable
class AddReelCommand {

    String name

    static constraints = {
        importFrom Reel, include: ['name']
    }
}
