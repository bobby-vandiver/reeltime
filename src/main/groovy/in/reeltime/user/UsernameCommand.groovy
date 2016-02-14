package in.reeltime.user

import grails.validation.Validateable

class UsernameCommand implements Validateable {

    String username

    static constraints = {
        username nullable: false, blank: false
    }
}
