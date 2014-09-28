package in.reeltime.activity

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController

import static javax.servlet.http.HttpServletResponse.*
import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static in.reeltime.common.ListMarshaller.marshallUserReelActivityList

class NewsfeedController extends AbstractController {

    def newsfeedService

    static allowedMethods = [listRecentActivity: 'GET']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('users-read') and #oauth2.hasScope('audiences-read')"])
    def listRecentActivity(Integer page) {
        int pageNumber = (page != null) ? page : 1

        if(pageNumberIsValid(pageNumber)) {
            def activities = newsfeedService.listRecentActivity(pageNumber)
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                [activities: marshallUserReelActivityList(activities)]
            }
        }
        else {
            errorMessageResponse('newsfeed.page.invalid', SC_BAD_REQUEST)
        }
    }

    private static boolean pageNumberIsValid(int pageNumber) {
        return pageNumber >= 1
    }
}
