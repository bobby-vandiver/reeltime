package in.reeltime.activity

import in.reeltime.reel.Reel
import in.reeltime.user.User

abstract class UserReelActivity {

    User user
    Reel reel

    Date dateCreated

    static constraints = {
        user nullable: false
        reel nullable: false
    }
}
