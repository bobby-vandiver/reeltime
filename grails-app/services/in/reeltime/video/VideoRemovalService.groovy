package in.reeltime.video

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User

class VideoRemovalService {

    def videoService
    def reelVideoManagementService

    def resourceRemovalService

    def videoStorageService
    def thumbnailStorageService
    def playlistAndSegmentStorageService

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

        def thumbnailBase = thumbnailStorageService.thumbnailBase

        log.info "Scheduling removal of master thumbnail for video [$videoId]"
        resourceRemovalService.scheduleForRemoval(thumbnailBase, video.masterThumbnailPath)

        log.info "Scheduling removal of thumbnails for video [$videoId]"
        video.thumbnails.each { thumbnail ->
            resourceRemovalService.scheduleForRemoval(thumbnailBase, thumbnail.uri)
        }

        def playlistAndSegmentBase = playlistAndSegmentStorageService.playlistBase

        log.info "Scheduling removal of playlists for video [$videoId]"
        video.playlistUris.each { playlistUri ->
            resourceRemovalService.scheduleForRemoval(playlistAndSegmentBase, playlistUri.uri)
        }

        log.info "Scheduling removal of video segments for video [$videoId]"
        video.playlists.each { playlist ->
            playlist.segments.each { segment ->
                resourceRemovalService.scheduleForRemoval(playlistAndSegmentBase, segment.uri)
            }
        }

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
