package test.helper

import in.reeltime.reel.Audience
import in.reeltime.reel.Reel
import in.reeltime.user.User

class ReelFactory {

    static Reel createReel(User owner, String name) {
        def reel = new Reel(name: name, audience: new Audience())
        owner.addToReels(reel)
        reel.save()
    }
}
