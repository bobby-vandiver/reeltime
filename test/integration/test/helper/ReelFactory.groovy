package test.helper

import in.reeltime.reel.Reel
import in.reeltime.reel.UserReel
import in.reeltime.user.User

class ReelFactory {

    static Reel createReel(User owner, String name) {
        def reel = new Reel(name: name).save()
        new UserReel(owner: owner, reel: reel).save()
        return reel
    }
}
