package in.reeltime.reel

import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.user.User
import in.reeltime.video.Video

class ReelService {

    def videoService
    def springSecurityService

    Reel createReel(User owner, String reelName) {
        def audience = new Audience(users: [])
        new Reel(owner: owner, name: reelName, audience: audience, videos: [])
    }

    Reel loadReel(Long reelId) {
        def reel = Reel.findById(reelId)
        if(!reel) {
            throw new ReelNotFoundException("Reel [$reelId] not found")
        }
        return reel
    }

    void addVideo(Long reelId, Long videoId) {

        def reel = loadReel(reelId)
        def video = videoService.loadVideo(videoId)

        if(!currentUserIsReelOwner(reel)) {
            throw new AuthorizationException("Only the owner of a reel can add videos to it")
        }

        reel.addToVideos(video)
        reel.save()
    }

    Collection<Video> listVideos(Long reelId) {
        loadReel(reelId).videos
    }

    private boolean currentUserIsReelOwner(Reel reel) {
        def currentUser = springSecurityService.currentUser as User
        return reel.owner == currentUser
    }
}
