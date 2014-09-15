package in.reeltime.activity

import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video

class ActivityService {

    void reelCreated(User user, Reel reel) {
        log.info "Reel [${reel.id}] has been created by user [${user.username}]"
        new CreateReelActivity(user: user, reel: reel).save()
    }

    void videoAddedToReel(User user, Reel reel, Video video) {
        log.info "Video [${video.id}] has been added to reel [${reel.id}] by user [${user.username}]"
        new AddVideoToReelActivity(user: user, reel: reel, video: video).save()
    }

    void deleteAllUserActivity(User user) {
        log.info "Deleting all activity for user [${user.username}]"
        def activities = UserReelActivity.findAllByUser(user)
        activities.each { activity ->
            deleteActivity(activity)
        }
    }

    void deleteActivity(UserReelActivity activity) {
        log.info "Deleting activity [${activity.id}]"
        activity.delete()
    }

    List<UserReelActivity> findActivities(List<User> users, List<Reel> reels) {
        UserReelActivity.findAllByUserInListOrReelInList(users, reels)
    }
}
