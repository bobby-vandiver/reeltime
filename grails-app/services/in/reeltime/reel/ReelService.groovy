package in.reeltime.reel

class ReelService {

    Reel createReel(String reelName) {
        def audience = new Audience(users: [])
        new Reel(name: reelName, audience: audience)
    }

}
