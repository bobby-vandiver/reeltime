package in.reeltime.video

class VideoController {

    def userAuthenticationService
    def videoService

    def upload() {

        if(hasValidParams()) {
            def creator = userAuthenticationService.loggedInUser
            def title = params.title
            def videoStream = request.getFile('video').inputStream

            videoService.createAndUploadVideo(creator, title, videoStream)
            render(status: 201)
        }
        else {
            render(status: 400, contentType: 'application/json') {
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
