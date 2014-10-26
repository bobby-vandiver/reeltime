package in.reeltime.search

import grails.validation.Validateable

@Validateable
class UsernamePagedListCommand extends PagedListCommand {

    String username

    static constraints = {
        username nullable: false, blank: false
    }
}
