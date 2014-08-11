package in.reeltime.video

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.ProbeException
import in.reeltime.exceptions.TranscoderException
import in.reeltime.user.User
import org.springframework.web.multipart.MultipartRequest

import static javax.servlet.http.HttpServletResponse.*

class VideoCreationController extends AbstractController {

    def springSecurityService
    def videoService
    def videoCreationService

    static allowedMethods = [upload: 'POST', status: 'GET']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('upload')"])
    def upload(VideoCreationCommand command) {
        bindAdditionalData(command)

        if(videoCreationService.allowCreation(command)) {
            def video = videoCreationService.createVideo(command)
            render(status: SC_ACCEPTED, contentType: JSON_CONTENT_TYPE) {
                [videoId: video.id]
            }
        }
        else {
            render(status: SC_BAD_REQUEST, contentType: JSON_CONTENT_TYPE) {
                [errors: localizedMessageService.getErrorMessages(command, request.locale)]
            }
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
        handleExceptionErrorMessageResponse(e, 'videoCreation.transcoder.error', SC_SERVICE_UNAVAILABLE)
    }

    def handleProbeException(ProbeException e) {
        handleExceptionErrorMessageResponse(e, 'videoCreation.probe.error', SC_SERVICE_UNAVAILABLE)
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('upload')"])
    def status(Long videoId) {
        int status
        if(!videoService.videoExists(videoId)) {
            status = SC_NOT_FOUND
        }
        else if(!videoService.currentUserIsVideoCreator(videoId)) {
            status = SC_FORBIDDEN
        }
        else if(!videoService.videoIsAvailable(videoId)) {
            status = SC_ACCEPTED
        }
        else {
            status = SC_CREATED
        }
        render(status: status)
    }
}
