package in.reeltime.user

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.UserNotFoundException

import static javax.servlet.http.HttpServletResponse.*

class UserFollowingController extends AbstractController {

    def userService
    def userFollowingService

    static allowedMethods = [followUser: 'POST', unfollowUser: 'DELETE']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('users-write')"])
    def followUser(String username) {
        handleSingleParamRequest(username, 'following.username.required') {
            def currentUser = userService.currentUser
            def userToFollow = userService.loadUser(username)

            userFollowingService.startFollowingUser(currentUser, userToFollow)
            render(status: SC_CREATED)
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('users-write')"])
    def unfollowUser(String username) {
        handleSingleParamRequest(username, 'following.username.required') {
            def currentUser = userService.currentUser
            def userToUnfollow = userService.loadUser(username)

            userFollowingService.stopFollowingUser(currentUser, userToUnfollow)
            render(status: SC_OK)
        }
    }

    def handleUserNotFoundException(UserNotFoundException e) {
        exceptionErrorMessageResponse(e, 'following.unknown.username', SC_BAD_REQUEST)
    }

    def handleIllegalArgumentException(IllegalArgumentException e) {
        exceptionErrorMessageResponse(e, 'following.invalid.request', SC_BAD_REQUEST)
    }
}
