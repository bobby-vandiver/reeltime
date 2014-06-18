package in.reeltime.video

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartRequest

import static javax.servlet.http.HttpServletResponse.*

class VideoCreationController {

    def userAuthenticationService
    def videoCreationService

    def messageSource

    static allowedMethods = [upload: 'POST']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('upload')"])
    def upload(VideoCreationCommand command) {
        bindAdditionalData(command)

        if(videoCreationService.allowCreation(command)) {
            videoCreationService.createVideo(command)
            render(status: SC_ACCEPTED)
        }
        else {
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [errors: getErrorMessages(command)]
            }
        }
    }

    private void bindAdditionalData(VideoCreationCommand command) {
        command.creator = userAuthenticationService.loggedInUser
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

    private List<String> getErrorMessages(VideoCreationCommand command) {
        def locale = Locale.default
        command.errors.allErrors.collect { error ->
            messageSource.getMessage(error, locale)
        }
    }
}
