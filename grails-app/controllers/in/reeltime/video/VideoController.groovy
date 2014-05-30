package in.reeltime.video

import grails.plugin.springsecurity.annotation.Secured

import static javax.servlet.http.HttpServletResponse.*

class VideoController {

    def userAuthenticationService
    def videoService

    static allowedMethods = [upload: 'POST']

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('upload')"])
    def upload() {

        if(hasValidParams()) {
            def creator = userAuthenticationService.loggedInUser
            def title = params.title
            def videoStream = request.getFile('video').inputStream

            videoService.createVideo(creator, title, videoStream)
            render(status: SC_CREATED)
        }
        else {
            render(status: SC_BAD_REQUEST, contentType: 'application/json') {
                [message: errorMessage]
            }
        }
    }

    private boolean hasValidParams() {
        params?.video && params?.title
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
