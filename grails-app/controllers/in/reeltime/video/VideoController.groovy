package in.reeltime.video

import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile

class VideoController {

    def userAuthenticationService
    def videoSubmissionService

    def upload() {

        if(!userLoggedIn) {
            render status: 401
        }
        else if(hasValidParams()) {
            videoSubmissionService.submit(video, videoStream)
            render status: 201
        }
        else {
            render(status: 400, contentType: 'application/json') {
                [message: errorMessage]
            }
        }
    }

    private boolean isUserLoggedIn() {
       return userAuthenticationService.isUserLoggedIn()
    }

    private boolean hasValidParams() {
        (params?.video != null) && (params?.title != null)
    }

    private Video getVideo() {
        new Video(title: params.title)
    }

    private InputStream getVideoStream() {
        MultipartFile video = request.getFile('video')
        return video.inputStream
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
