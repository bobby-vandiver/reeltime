package in.reeltime.video

import in.reeltime.user.User

class VideoService {

    def springSecurityService

    boolean currentUserIsVideoCreator(Long videoId) {
        def currentUser = springSecurityService.currentUser as User
        Video.findByIdAndCreator(videoId, currentUser) != null
    }

    boolean videoExists(Long videoId) {
        Video.findById(videoId) != null
    }

    boolean videoIsAvailable(Long videoId) {
        def video = Video.findById(videoId)
        return video?.available ?: false
    }
}
