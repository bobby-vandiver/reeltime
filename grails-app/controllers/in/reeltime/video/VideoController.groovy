package in.reeltime.video

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.ProbeException
import in.reeltime.exceptions.TranscoderException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.search.PagedListCommand
import in.reeltime.user.User
import org.springframework.web.multipart.MultipartRequest
import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class VideoController extends AbstractController {

    def springSecurityService

    def videoService
    def videoCreationService
    def videoRemovalService

    static allowedMethods = [listVideos: 'GET', upload: 'POST', status: 'GET', remove: 'DELETE']

    @Secured(["#oauth2.hasScope('videos-read')"])
    def listVideos(PagedListCommand command) {
        log.debug "Listing all videos on page [${command.page}]"
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(videoService.listVideos(command.page))
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('videos-write')"])
    def upload(VideoCreationCommand command) {
        bindAdditionalData(command)

        if(videoCreationService.allowCreation(command)) {
            render(status: SC_ACCEPTED, contentType: APPLICATION_JSON) {
                marshall(videoCreationService.createVideo(command))
            }
        }
        else {
            commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
    }

    private void bindAdditionalData(VideoCreationCommand command) {
        command.creator = springSecurityService.currentUser as User
        command.videoStream = getVideoStreamFromRequest()
    }

    private InputStream getVideoStreamFromRequest() {
        if(request instanceof MultipartRequest) {
            return request.getFile('video')?.inputStream
        }
        else {
            return null
        }
    }

    def handleTranscoderException(TranscoderException e) {
        exceptionErrorMessageResponse(e, 'videoCreation.transcoder.error', SC_SERVICE_UNAVAILABLE)
    }

    def handleProbeException(ProbeException e) {
        exceptionErrorMessageResponse(e, 'videoCreation.probe.error', SC_SERVICE_UNAVAILABLE)
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('videos-write')"])
    def status(VideoCommand command) {
        handleCommandRequest(command) {
            int status
            def videoId = command.videoId
            if (!videoService.videoExists(videoId)) {
                status = SC_NOT_FOUND
            } else if (!videoService.currentUserIsVideoCreator(videoId)) {
                status = SC_FORBIDDEN
            } else if (!videoService.videoIsAvailable(videoId)) {
                status = SC_ACCEPTED
            } else {
                status = SC_CREATED
            }
            render(status: status)
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('videos-write')"])
    def remove(VideoCommand command) {
        log.debug "Removing video [${command.videoId}]"
        handleCommandRequest(command) {
            videoRemovalService.removeVideoById(command.videoId)
            render(status: SC_OK)
        }
    }

    def handleVideoNotFoundException(VideoNotFoundException e) {
        exceptionErrorMessageResponse(e, 'video.unknown', SC_NOT_FOUND)
    }
}
