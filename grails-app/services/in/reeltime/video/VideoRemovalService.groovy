package in.reeltime.video

class VideoRemovalService {

    def reelVideoManagementService
    def resourceRemovalService

    /*
        1. Remove video from all reels
        2. Collect all uris for playlists and segments
        3. Create removal targets for uris
        4. Delete video instance
    */

    void removeVideo(Video video) {

        reelVideoManagementService.removeVideoFromAllReels(video)
    }
}
