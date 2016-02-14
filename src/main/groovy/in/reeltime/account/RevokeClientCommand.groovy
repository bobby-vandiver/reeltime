package in.reeltime.account

import grails.validation.Validateable

class RevokeClientCommand implements Validateable {

    String client_id

    static constraints = {
        client_id nullable: false, blank: false
    }
}
