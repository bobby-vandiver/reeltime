package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.video.Video

class ReelVideoManagementService {

    def reelService
    def reelAuthorizationService

    def videoService

    Collection<Video> listVideos(Long reelId) {
        def reel = reelService.loadReel(reelId)
        ReelVideo.findAllByReel(reel)?.video ?: []
    }

    void addVideo(Long reelId, Long videoId) {

        def reel = reelService.loadReel(reelId)
        def video = videoService.loadVideo(videoId)

        addVideoToReel(reel, video)
    }

    void addVideoToReel(Reel reel, Video video) {

        if(!reelAuthorizationService.currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Only the owner of a reel can add videos to it")
        }

        updateReelAndVideo(reel, video)

        // TODO: Guard against duplication
        new ReelVideo(reel: reel, video: video).save()
    }

    void removeVideo(Long reelId, Long videoId) {

        def reel = reelService.loadReel(reelId)
        def video = videoService.loadVideo(videoId)

        if(!reelAuthorizationService.currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Only the owner of a reel can remove videos from it")
        }
        else if(!reelContainsVideo(reel, video)) {
            throw new VideoNotFoundException("Reel [$reelId] does not contain video [$videoId]")
        }

        removeVideoFromReel(reel, video)
    }

    private static boolean reelContainsVideo(Reel reel, Video video) {
        ReelVideo.findByReelAndVideo(reel, video) != null
    }

    void removeVideoFromAllReels(Video video) {

        def reelsVideoBelongsTo = ReelVideo.findAllByVideo(video)?.reel ?: []

        reelsVideoBelongsTo.each { Reel reel ->
            removeVideoFromReel(reel, video)
        }

        log.info "Marking video [${video.id}] as unavailable"
        video.available = false
        videoService.storeVideo(video)
    }

    private void removeVideoFromReel(Reel reel, Video video) {
        updateReelAndVideo(reel, video)

        def reelVideo = ReelVideo.findByReelAndVideo(reel, video)
        reelVideo.delete()
    }

    private void updateReelAndVideo(Reel reel, Video video) {
        reelService.storeReel(reel)
        videoService.storeVideo(video)
    }
}
