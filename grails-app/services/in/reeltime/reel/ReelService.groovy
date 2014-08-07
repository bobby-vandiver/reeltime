package in.reeltime.reel

import in.reeltime.user.User
import in.reeltime.video.Video

class ReelService {

    Reel createReel(User owner, String reelName) {
        def audience = new Audience(users: [])
        new Reel(owner: owner, name: reelName, audience: audience, videos: [])
    }

    void addVideo(Long reelId, Long videoId) {
        def reel = Reel.findById(reelId)
        def video = Video.findById(videoId)
        reel.addToVideos(video)
        reel.save()
    }

    Collection<Video> listVideos(Long reelId) {
        Reel.findById(reelId).videos
    }
}
