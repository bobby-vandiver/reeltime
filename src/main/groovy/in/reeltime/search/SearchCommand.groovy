package in.reeltime.search

import grails.validation.Validateable

class SearchCommand extends PagedListCommand implements Validateable {

    String type
    String query

    static constraints = {
        type nullable: false, blank: false, inList: ['user', 'video', 'reel']
        query nullable: false, blank: false
    }
}
