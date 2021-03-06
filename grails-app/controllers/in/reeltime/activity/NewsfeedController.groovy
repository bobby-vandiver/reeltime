package in.reeltime.activity

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.search.PagedListCommand

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.SC_OK

class NewsfeedController extends AbstractController {

    def newsfeedService

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('users-read') and #oauth2.hasScope('audiences-read')"])
    def listRecentActivity(PagedListCommand command) {
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                [activities: marshall(newsfeedService.listRecentActivity(command.page))]
            }
        }
    }
}
