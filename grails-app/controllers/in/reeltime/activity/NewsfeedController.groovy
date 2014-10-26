package in.reeltime.activity

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.search.PagedListCommand

import static javax.servlet.http.HttpServletResponse.*
import static in.reeltime.common.ContentTypes.APPLICATION_JSON

class NewsfeedController extends AbstractController {

    def newsfeedService

    static allowedMethods = [listRecentActivity: 'GET']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('users-read') and #oauth2.hasScope('audiences-read')"])
    def listRecentActivity(PagedListCommand command) {
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                [activities: marshall(newsfeedService.listRecentActivity(command.page))]
            }
        }
    }
}
