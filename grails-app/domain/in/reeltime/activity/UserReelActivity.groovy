package in.reeltime.activity

import in.reeltime.reel.Reel
import in.reeltime.user.User

class UserReelActivity extends UserActivity {

    Reel reel

    static constraints = {
        reel nullable: false
    }
}
