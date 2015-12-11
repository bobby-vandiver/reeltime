package in.reeltime.user

import grails.validation.Validateable

@Validateable(nullable = true)
class UsernameCommand {

    String username

    static constraints = {
        username nullable: false, blank: false
    }
}
