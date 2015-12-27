package in.reeltime.video

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User

class VideoRemovalService {

    def videoService
    def reelVideoManagementService

    def resourceRemovalService

    def thumbnailRemovalService
    def playlistRemovalService

    def videoStorageService
    def thumbnailStorageService

    def transcoderJobService

    def authenticationService
    def userService

    void removeVideoById(Long videoId) {
        def video = videoService.loadVideo(videoId)
        removeVideo(video)
    }

    void removeVideo(Video video) {
        def creator = video.creator

        if (authenticationService.currentUser != creator) {
            throw new AuthorizationException("Only the creator of a video can delete it")
        }

        def videoId = video.id

        log.info "Removing video [$videoId] from all reels"
        reelVideoManagementService.removeVideoFromAllReels(video)

        log.info "Scheduling removal of master video for video [$videoId]"
        def masterVideoBase = videoStorageService.videoBase
        resourceRemovalService.scheduleForRemoval(masterVideoBase, video.masterPath)

        log.info "Removing thumbnails for video [$videoId]"
        thumbnailRemovalService.removeThumbnailsForVideo(video)

        log.info "Removing playlists for video [$videoId]"
        playlistRemovalService.removePlaylistsForVideo(video)

        log.info "Removing the transcoder jobs associated with video [$videoId]"
        transcoderJobService.removeJobForVideo(video)

        log.info "Removing video [$videoId] from creator [${creator.username}]"
        creator.removeFromVideos(video)
        userService.storeUser(creator)

        log.info "Deleting video [$videoId]"
        video.delete()
    }

    void removeVideosForUser(User user) {
        def videosToRemove = []
        if(user?.videos) {
            videosToRemove.addAll(user.videos)
        }

        videosToRemove.each { video ->
            log.debug "Removing video [${video.id}]"
            removeVideo(video)
        }
    }
}
