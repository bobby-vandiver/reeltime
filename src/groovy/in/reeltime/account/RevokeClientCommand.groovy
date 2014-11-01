package in.reeltime.account

import grails.validation.Validateable

@Validateable
class RevokeClientCommand {

    String client_id

    static constraints = {
        client_id nullable: false, blank: false
    }
}
