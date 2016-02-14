package in.reeltime.search

import grails.validation.Validateable

class PagedListCommand implements Validateable {

    private static final Integer DEFAULT_PAGE = 1

    Integer page

    static constraints = {
        page nullable: false, min: DEFAULT_PAGE
    }

    def beforeValidate() {
        page = (page != null) ? page : DEFAULT_PAGE
    }
}
