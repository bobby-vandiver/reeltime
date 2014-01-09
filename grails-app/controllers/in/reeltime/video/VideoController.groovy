package in.reeltime.video

import static javax.servlet.http.HttpServletResponse.*

class VideoController {

    def userAuthenticationService
    def videoService

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
            message = 'Video is required'
        }
        else if(!params?.title) {
            message = 'Title is required'
        }
        return message
    }
}
