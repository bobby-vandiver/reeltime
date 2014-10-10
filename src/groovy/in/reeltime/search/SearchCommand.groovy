package in.reeltime.search

import grails.validation.Validateable

@Validateable
class SearchCommand extends PagedListCommand {

    String type
    String query

    static constraints = {
        type nullable: false, blank: false, inList: ['user', 'video', 'reel']
        query nullable: false, blank: false
    }
}
