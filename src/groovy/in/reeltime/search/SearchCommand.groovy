package in.reeltime.search

import grails.validation.Validateable

@Validateable
class SearchCommand {

    private static final Integer DEFAULT_PAGE = 1

    String type
    String query
    Integer page = DEFAULT_PAGE

    static constraints = {
        type nullable: false, blank: false, inList: ['user', 'video', 'reel']
        query nullable: false, blank: false
        page nullable: false, min: 1
    }
}
