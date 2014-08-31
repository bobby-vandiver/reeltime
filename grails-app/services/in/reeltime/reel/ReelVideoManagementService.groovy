package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.video.Video

class ReelVideoManagementService {

    def reelService
    def reelAuthorizationService

    def videoService

    Collection<Video> listVideos(Long reelId) {
        reelService.loadReel(reelId).videos
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

        reel.addToVideos(video)
        video.addToReels(reel)

        updateReelAndVideo(reel, video)
    }

    void removeVideo(Long reelId, Long videoId) {

        def reel = reelService.loadReel(reelId)
        def video = videoService.loadVideo(videoId)

        if(!reelAuthorizationService.currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Only the owner of a reel can remove videos from it")
        }
        else if(!reel.containsVideo(video)) {
            throw new VideoNotFoundException("Reel [$reelId] does not contain video [$videoId]")
        }

        removeVideoFromReel(reel, video)
    }

    void removeVideoFromAllReels(Video video) {

        def reelsVideoBelongsTo = []
        video.reels?.each { Reel reel ->
            reelsVideoBelongsTo << reel
        }

        reelsVideoBelongsTo.each { Reel reel ->
            removeVideoFromReel(reel, video)
        }

        log.info "Marking video [${video.id}] as unavailable"
        video.available = false
        videoService.storeVideo(video)
    }

    private void removeVideoFromReel(Reel reel, Video video) {
        reel.removeFromVideos(video)
        video.removeFromReels(reel)

        updateReelAndVideo(reel, video)
    }

    private void updateReelAndVideo(Reel reel, Video video) {
        reelService.storeReel(reel)
        videoService.storeVideo(video)
    }
}
