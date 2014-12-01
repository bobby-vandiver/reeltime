package in.reeltime.activity

import in.reeltime.reel.Reel

class UserReelActivity extends UserActivity {

    Reel reel

    static constraints = {
        reel nullable: false
    }

    @Override
    public String toString() {
        return super.toString() + " " +
                "UserReelActivity{" +
                "id=" + id +
                ", reel=" + reel +
                ", version=" + version +
                '}';
    }
}
