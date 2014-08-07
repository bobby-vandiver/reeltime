package in.reeltime.reel

import in.reeltime.user.User
import in.reeltime.video.Video

class ReelService {

    Reel createReel(User owner, String reelName) {
        def audience = new Audience(users: [])
        new Reel(owner: owner, name: reelName, audience: audience, videos: [])
    }

    // TODO: Ensure current user is the owner of the reel
    void addVideo(Long reelId, Long videoId) {
        def reel = Reel.findById(reelId)
        def video = Video.findById(videoId)
        reel.addToVideos(video)
        reel.save()
    }

    Collection<Reel> listReels(String username) {
        User.findByUsername(username).reels
    }

    Collection<Video> listVideos(Long reelId) {
        Reel.findById(reelId).videos
    }
}
