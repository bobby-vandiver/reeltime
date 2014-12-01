package in.reeltime.activity

import groovy.transform.ToString
import in.reeltime.video.Video

@ToString(includeNames = true, includeSuper = true)
class UserReelVideoActivity extends UserReelActivity {

    Video video

    static constraints = {
        video nullable: false
    }
}
