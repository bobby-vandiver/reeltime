package in.reeltime.video

import org.springframework.web.multipart.MultipartFile

class VideoController {

    def userAuthenticationService
    def videoSubmissionService

    def upload() {

        if(!userAuthenticationService.isUserLoggedIn()) {
            render status: 401
        }
        else if(params.video) {
            videoSubmissionService.submit(video, videoStream)
            render status: 201
        }
        else {
            render status: 400
        }
    }

    private Video getVideo() {
        new Video(title: params.title)
    }

    private InputStream getVideoStream() {
        MultipartFile video = request.getFile('video')
        return video.inputStream
    }
}
