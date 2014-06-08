package in.reeltime.video

import grails.plugin.springsecurity.annotation.Secured

import static javax.servlet.http.HttpServletResponse.*

class VideoCreationController {

    def userAuthenticationService
    def videoCreationService

    static allowedMethods = [upload: 'POST']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('upload')"])
    def upload(VideoCreationCommand command) {
        bindAdditionalData(command)

        if(videoCreationService.allowCreation(command)) {
            videoCreationService.createVideo(command)
            render(status: SC_CREATED)
        }
        else {
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [message: errorMessage]
            }
        }
    }

    private void bindAdditionalData(VideoCreationCommand command) {
        command.creator = userAuthenticationService.loggedInUser
        command.videoStream = request.getFile('video')?.inputStream
    }

    private String getErrorMessage() {
        def message = ''

        if(!params?.video) {
            message = '[video] is required'
        }
        else if(!params?.title) {
            message = '[title] is required'
        }
        return message
    }
}
