package in.reeltime.user

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.search.PagedListCommand

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.SC_OK

class UserController extends AbstractController {

    def userService

    @Secured(["#oauth2.hasScope('users-read')"])
    def getUser(UsernameCommand command) {
        log.debug "Getting details for user [${command.username}]"
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(userService.loadUser(command.username))
            }
        }
    }

    @Secured(["#oauth2.hasScope('users-read')"])
    def listUsers(PagedListCommand command) {
        log.debug "Listing all users on page [${command.page}]"
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(users: userService.listUsers(command.page))
            }
        }
    }
}
