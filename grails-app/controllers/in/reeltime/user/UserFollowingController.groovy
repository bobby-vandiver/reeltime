package in.reeltime.user

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.search.UsernamePagedListCommand

import static javax.servlet.http.HttpServletResponse.*
import static in.reeltime.common.ContentTypes.APPLICATION_JSON

class UserFollowingController extends AbstractController {

    def userService
    def userFollowingService
    def authenticationService

    static allowedMethods = [
            followUser: 'POST', unfollowUser: 'DELETE',
            listFollowers: 'GET', listFollowees: 'GET'
    ]

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('users-write')"])
    def followUser(String username) {
        handleSingleParamRequest(username, 'following.username.required') {
            def currentUser = authenticationService.currentUser
            def userToFollow = userService.loadUser(username)

            userFollowingService.startFollowingUser(currentUser, userToFollow)
            render(status: SC_CREATED)
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('users-write')"])
    def unfollowUser(String username) {
        handleSingleParamRequest(username, 'following.username.required') {
            def currentUser = authenticationService.currentUser
            def userToUnfollow = userService.loadUser(username)

            userFollowingService.stopFollowingUser(currentUser, userToUnfollow)
            render(status: SC_OK)
        }
    }

    @Secured(["#oauth2.hasScope('users-read')"])
    def listFollowers(UsernamePagedListCommand command) {
        log.debug "Listing followers for user [${command.username}] on page [${command.page}]"
        handleCommandRequest(command) {
            def user = userService.loadUser(command.username)
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(userFollowingService.listFollowersForFollowee(user, command.page))
            }
        }
    }

    @Secured(["#oauth2.hasScope('users-read')"])
    def listFollowees(UsernamePagedListCommand command) {
        log.debug "Listing followees for user [${command.username}] on page [${command.page}]"
        handleCommandRequest(command) {
            def user = userService.loadUser(command.username)
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(userFollowingService.listFolloweesForFollower(user, command.page))
            }
        }
    }

    def handleUserNotFoundException(UserNotFoundException e) {
        exceptionErrorMessageResponse(e, 'following.unknown.username', SC_NOT_FOUND)
    }

    def handleIllegalArgumentException(IllegalArgumentException e) {
        exceptionErrorMessageResponse(e, 'following.invalid.request', SC_BAD_REQUEST)
    }
}
