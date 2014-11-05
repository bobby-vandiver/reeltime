package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.video.Video

class ReelVideoManagementService {

    def reelService
    def reelAuthorizationService

    def videoService

    def authenticationService
    def activityService

    def maxVideosPerPage

    List<Video> listVideosInReel(Long reelId, int page) {
        def reel = reelService.loadReel(reelId)
        def videoIds = ReelVideo.findAllByReel(reel)?.collect { it.video*.id }?.flatten()

        int offset = (page - 1) * maxVideosPerPage
        def params = [max: maxVideosPerPage, offset: offset, sort: 'dateCreated', order: 'desc']

        Video.findAllByIdInListAndAvailable(videoIds, true, params)
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
        else if(reel.containsVideo(video)) {
            throw new AuthorizationException("Cannot add a video to a reel multiple times")
        }

        updateReelAndVideo(reel, video)
        new ReelVideo(reel: reel, video: video).save()

        def currentUser = authenticationService.currentUser
        activityService.videoAddedToReel(currentUser, reel, video)
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

    void removeAllVideosFromReel(Reel reel) {
        def reelVideos = ReelVideo.findAllByReel(reel)

        reelVideos.each { reelVideo ->
            def video = reelVideo.video
            removeVideoFromReel(reel, video)
        }
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

    void removeVideoFromReel(Reel reel, Video video) {
        updateReelAndVideo(reel, video)
        ReelVideo.findByReelAndVideo(reel, video)?.delete()
        activityService.videoRemovedFromReel(reel.owner, reel, video)
    }

    private void updateReelAndVideo(Reel reel, Video video) {
        reelService.storeReel(reel)
        videoService.storeVideo(video)
    }
}
