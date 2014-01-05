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
            MultipartFile video = request.getFile('video')
            videoSubmissionService.submit(video.inputStream)
            render status: 201
        }
        else {
            render status: 400
        }
    }
}
