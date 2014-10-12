package in.reeltime.reel

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.exceptions.VideoNotFoundException

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class ReelController extends AbstractController {

    def reelService
    def reelVideoManagementService

    static allowedMethods = [
            listUserReels: 'GET', addReel: 'POST', deleteReel: 'DELETE',
            listVideos: 'GET', addVideo: 'POST', removeVideo: 'DELETE'
    ]

    @Secured(["#oauth2.hasScope('reels-read')"])
    def listUserReels(String username) {
        log.debug "Listing reels for user [$username]"
        handleSingleParamRequest(username, 'reel.username.required') {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(reelService.listReelsByUsername(username))
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('reels-write')"])
    def addReel(String name) {
        log.debug "Adding reel [$name]"
        handleSingleParamRequest(name, 'reel.name.required') {
            render(status: SC_CREATED, contentType: APPLICATION_JSON) {
                marshall(reelService.addReel(name))
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('reels-write')"])
    def deleteReel(Long reelId) {
        log.debug "Deleting reel [$reelId]"
        handleSingleParamRequest(reelId, 'reel.id.required') {
            reelService.deleteReel(reelId)
            render(status: SC_OK)
        }
    }

    @Secured(["#oauth2.hasScope('reels-read')"])
    def listVideos(Long reelId) {
        log.debug "Listing videos in reel [$reelId]"
        handleSingleParamRequest(reelId, 'reel.id.required') {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(reelVideoManagementService.listVideos(reelId))
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('reels-write')"])
    def addVideo(Long reelId, Long videoId) {
        log.debug "Adding video [$videoId] to reel [$reelId]"
        if(!reelId) {
            errorMessageResponse('reel.id.required', SC_BAD_REQUEST)
        }
        else if(!videoId) {
            errorMessageResponse('video.id.required', SC_BAD_REQUEST)
        }
        else {
            reelVideoManagementService.addVideo(reelId, videoId)
            render(status: SC_CREATED)
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('reels-write')"])
    def removeVideo(Long reelId, Long videoId) {
        log.debug "Removing video [$videoId] from reel [$reelId]"
        if(!reelId) {
            errorMessageResponse('reel.id.required', SC_BAD_REQUEST)
        }
        else if(!videoId) {
            errorMessageResponse('video.id.required', SC_BAD_REQUEST)
        }
        else {
            reelVideoManagementService.removeVideo(reelId, videoId)
            render(status: SC_OK)
        }
    }

    def handleAuthorizationException(AuthorizationException e) {
        exceptionErrorMessageResponse(e, 'reel.unauthorized', SC_FORBIDDEN)
    }

    def handleUserNotFoundException(UserNotFoundException e) {
        exceptionErrorMessageResponse(e, 'reel.unknown.username', SC_NOT_FOUND)
    }

    def handleReelNotFoundException(ReelNotFoundException e) {
        exceptionErrorMessageResponse(e, 'reel.unknown', SC_NOT_FOUND)
    }

    def handleInvalidReelNameException(InvalidReelNameException e) {
        exceptionErrorMessageResponse(e, 'reel.invalid.name', SC_BAD_REQUEST)
    }

    def handleVideoNotFoundException(VideoNotFoundException e) {
        exceptionErrorMessageResponse(e, 'video.unknown', SC_NOT_FOUND)
    }
}
