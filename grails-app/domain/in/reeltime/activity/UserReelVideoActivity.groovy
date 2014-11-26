package in.reeltime.activity

import in.reeltime.video.Video

class UserReelVideoActivity extends UserReelActivity {

    Video video

    static constraints = {
        video nullable: false
    }

    @Override
    public String toString() {
        return super.toString() + " " +
                "UserReelVideoActivity{" +
                "id=" + id +
                ", video=" + video +
                ", version=" + version +
                '}';
    }
}
