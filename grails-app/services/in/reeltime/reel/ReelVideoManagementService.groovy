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
        reelService.storeReel(reel)
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

        reel.removeFromVideos(video)
        reelService.storeReel(reel)
    }

    void removeVideoFromAllReels(Video video) {
//
//        def reels = Reel.findAllByVideosInList([])
//
//        reels.each { reel ->
//            reel.removeFromVideos(video)
//            reel.save()
//        }
    }
}
