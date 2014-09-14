package in.reeltime.activity

import in.reeltime.video.Video

class AddVideoToReelActivity extends UserReelActivity {

    Video video

    @Override
    ActivityType getType() {
        return ActivityType.AddVideoToReel
    }

    static constraints = {
        video nullable: false
    }
}
