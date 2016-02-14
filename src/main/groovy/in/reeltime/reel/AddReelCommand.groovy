package in.reeltime.reel

import grails.validation.Validateable

class AddReelCommand implements Validateable {

    def reelAuthorizationService

    String name

    static constraints = {
        importFrom Reel, include: ['name']
        name validator: reservedNameValidator
    }

    private static Closure reservedNameValidator = { String val, obj ->
        if(obj.reelAuthorizationService.reelNameIsReserved(val)) {
            return 'reserved'
        }
    }
}
