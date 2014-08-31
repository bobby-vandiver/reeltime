package in.reeltime.video

class VideoRemovalService {

    def reelVideoManagementService
    def resourceRemovalService
    def pathGenerationService

    void removeVideo(Video video) {
        def videoId = video.id

        log.info "Removing video [$videoId] from all reels"
        reelVideoManagementService.removeVideoFromAllReels(video)

        log.info "Scheduling removal of master video for video [$videoId]"
        def masterVideoBase = pathGenerationService.inputBase as String
        resourceRemovalService.scheduleForRemoval(masterVideoBase, video.masterPath)

        def playlistAndSegmentBase = pathGenerationService.outputBase

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

        log.info "Deleting video [$videoId]"
        video.delete()
    }
}
