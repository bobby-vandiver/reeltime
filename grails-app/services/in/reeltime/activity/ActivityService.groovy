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

    void reelDeleted(User user, Reel reel) {
        log.info "Reel [${reel.id}] has been been deleted by user [${user.username}]"
        CreateReelActivity.findByUserAndReel(user, reel)?.delete()
    }

    void videoRemovedFromReel(User user, Reel reel, Video video) {
        log.info "Video [${video.id}] has been removed from reel [${reel.id}] by user [${user.username}]"
        AddVideoToReelActivity.findByReelAndVideo(reel, video)?.delete()
    }

    void deleteAllUserActivity(User user) {
        log.info "Deleting all activity for user [${user.username}]"
        UserReelActivity.findAllByUser(user)*.delete()
    }

    List<UserReelActivity> findActivities(List<User> users, List<Reel> reels) {
        UserReelActivity.findAllByUserInListOrReelInList(users, reels)
    }
}
