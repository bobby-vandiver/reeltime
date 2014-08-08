package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.video.Video

class ReelVideoManagementService {

    def reelService
    def videoService

    Collection<Video> listVideos(Long reelId) {
        reelService.loadReel(reelId).videos
    }

    void addVideo(Long reelId, Long videoId) {

        def reel = reelService.loadReel(reelId)
        def video = videoService.loadVideo(videoId)

        if(reelService.currentUserIsNotReelOwner(reel)) {
            throw new AuthorizationException("Only the owner of a reel can add videos to it")
        }

        reel.addToVideos(video)
        reel.save()
    }

    void removeVideo(Long reelId, Long videoId) {

        def reel = reelService.loadReel(reelId)
        def video = videoService.loadVideo(videoId)

        if(reelService.currentUserIsNotReelOwner(reel)) {
            throw new AuthorizationException("Only the owner of a reel can remove videos from it")
        }
        else if(!reel.containsVideo(video)) {
            throw new VideoNotFoundException("Reel [$reelId] does not contain video [$videoId]")
        }

        reel.removeFromVideos(video)
        reel.save()
    }
}
