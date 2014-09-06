package in.reeltime.video

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.VideoNotFoundException

import static javax.servlet.http.HttpServletResponse.*

class VideoRemovalController extends AbstractController {

    def videoRemovalService

    static allowedMethods = [remove: 'DELETE']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('videos-write')"])
    def remove(Long videoId) {
        log.debug "Removing video [$videoId]"
        handleSingleParamRequest(videoId, 'video.id.required') {
            videoRemovalService.removeVideoById(videoId)
            render(status: SC_OK)
        }
    }

    def handleVideoNotFoundException(VideoNotFoundException e) {
        exceptionErrorMessageResponse(e, 'video.unknown', SC_NOT_FOUND)
    }
}
