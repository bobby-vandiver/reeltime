package in.reeltime.activity

import in.reeltime.reel.Reel
import in.reeltime.user.User

abstract class UserReelActivity {

    User user
    Reel reel

    Date dateCreated

    protected abstract ActivityType getType()

    static transients = ['type']

    static constraints = {
        user nullable: false
        reel nullable: false
    }
}
