package in.reeltime.activity

import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video

class ActivityService {

    void reelCreated(User user, Reel reel) {
        new CreateReelActivity(user: user, reel: reel).save()
    }

    void videoAddedToReel(User user, Reel reel, Video video) {
        new AddVideoToReelActivity(user: user, reel: reel, video: video).save()
    }

    void deleteActivity(UserReelActivity activity) {
        activity.delete()
    }

    List<UserReelActivity> findActivities(List<User> users, List<Reel> reels) {
        UserReelActivity.findAllByUserInListOrReelInList(users, reels)
    }
}
