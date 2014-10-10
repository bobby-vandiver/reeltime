package in.reeltime.search

import grails.validation.Validateable

@Validateable
class PagedListCommand {

    private static final Integer DEFAULT_PAGE = 1

    Integer page = DEFAULT_PAGE

    static constraints = {
        page nullable: false, min: DEFAULT_PAGE
    }
}
