package in.reeltime.video

import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.user.User

class VideoService {

    // TODO: Get current user from the userService
    def springSecurityService
    def maxVideosPerPage

    boolean currentUserIsVideoCreator(Long videoId) {
        def currentUser = springSecurityService.currentUser as User
        Video.findByIdAndCreator(videoId, currentUser) != null
    }

    boolean videoExists(Long videoId) {
        Video.findById(videoId) != null
    }

    List<Video> listVideos(int page) {
        int offset = (page - 1) * maxVideosPerPage
        Video.findAllByAvailable(true, [max: maxVideosPerPage, offset: offset, sort: 'dateCreated'])
    }

    Video loadVideo(Long videoId) {
        def video = Video.findById(videoId)
        if(!video) {
            throw new VideoNotFoundException("Video [$videoId] not found")
        }
        return video
    }

    void storeVideo(Video video) {
        video.save()
    }

    boolean videoIsAvailable(Long videoId) {
        def video = Video.findById(videoId)
        return video?.available ?: false
    }
}
