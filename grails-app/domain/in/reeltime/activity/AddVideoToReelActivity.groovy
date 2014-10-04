package in.reeltime.activity

import in.reeltime.video.Video

class AddVideoToReelActivity extends UserReelActivity {

    Video video

    static constraints = {
        video nullable: false
    }
}
