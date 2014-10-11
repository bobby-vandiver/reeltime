package in.reeltime.user

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.search.PagedListCommand
import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class UserController extends AbstractController {

    def userService

    static allowedMethods = [listUsers: 'GET']

    @Secured(["#oauth2.hasScope('users-read')"])
    def listUsers(PagedListCommand command) {

        if(!command.hasErrors()) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(userService.listUsers(command.page))
            }
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
    }
}
