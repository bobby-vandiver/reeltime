package in.reeltime.reel

import in.reeltime.user.User

class ReelService {

    Reel createReel(User owner, String reelName) {
        def audience = new Audience(users: [])
        new Reel(owner: owner, name: reelName, audience: audience, videos: [])
    }

}
