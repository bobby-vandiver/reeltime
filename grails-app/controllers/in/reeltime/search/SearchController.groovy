package in.reeltime.search

import in.reeltime.common.AbstractController

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import static javax.servlet.http.HttpServletResponse.SC_OK
import static in.reeltime.common.ContentTypes.APPLICATION_JSON

class SearchController extends AbstractController {

    def userSearchService
    def videoSearchService
    def reelSearchService

    def search(SearchCommand command) {

        if(!command.hasErrors()) {
            def searchService = getSearchServiceForType(command.type)
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(searchService.search(command.query, command.page))
            }
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
    }

    private SearchService getSearchServiceForType(String type) {
        switch (type) {
            case 'user':
                return userSearchService

            case 'video':
                return videoSearchService

            case 'reel':
                return reelSearchService

            default:
                throw new IllegalArgumentException("Could not find search service for type [$type]")
        }
    }
}
