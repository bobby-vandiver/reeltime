package in.reeltime.activity

import groovy.transform.ToString
import in.reeltime.reel.Reel

@ToString(includeNames = true, includeSuper = true)
class UserReelActivity extends UserActivity {

    Reel reel

    static constraints = {
        reel nullable: false
    }
}
