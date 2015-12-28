package in.reeltime.activity

import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video

class ActivityService {

    def maxActivitiesPerPage

    void reelCreated(User user, Reel reel) {
        if(createReelActivityExists(reel)) {
            throw new IllegalArgumentException("Reel creation activity already exists for reel [${reel.id}]")
        }
        log.info "Reel [${reel.id}] has been created by user [${user.username}]"
        new UserReelActivity(user: user, reel: reel, type: ActivityType.CreateReel.value).save()
    }

    private boolean createReelActivityExists(Reel reel) {
        UserReelActivity.findByReelAndType(reel, ActivityType.CreateReel.value) != null
    }

    void reelDeleted(User user, Reel reel) {
        log.info "Reel [${reel.id}] has been been deleted by user [${user.username}]"
        UserReelActivity.findByUserAndReelAndType(user, reel, ActivityType.CreateReel.value)?.delete()
    }

    void userJoinedAudience(User user, Reel reel) {
        if(!createReelActivityExists(reel) && !reel.isUncategorizedReel()) {
            throw new IllegalArgumentException("Create reel activity must exist before a join reel audience activity can be created for reel [${reel.id}]")
        }
        if(UserReelActivity.findByUserAndReelAndType(user, reel, ActivityType.JoinReelAudience.value)) {
            throw new IllegalArgumentException("Join reel audience activity already exists for reel [${reel.id}]")
        }
        log.info "User [${user.username}] has joined the audience for reel [${reel.id}]"
        new UserReelActivity(user: user, reel: reel, type: ActivityType.JoinReelAudience.value).save()
    }

    void userLeftAudience(User user, Reel reel) {
        log.info "User [${user.username}] has left the audience for reel [${reel.id}]"
        UserReelActivity.findByUserAndReelAndType(user, reel, ActivityType.JoinReelAudience.value)?.delete()
    }

    void videoAddedToReel(User user, Reel reel, Video video) {
        if(!createReelActivityExists(reel) && !reel.isUncategorizedReel()) {
            throw new IllegalArgumentException("Create reel activity must exist before a video added to reel activity can be created for reel [${reel.id}]")
        }
        if(UserReelVideoActivity.findByUserAndReelAndVideo(user, reel, video)) {
            throw new IllegalArgumentException("Video added to reel activity already exists for reel [${reel.id}]")
        }
        log.info "Video [${video.id}] has been added to reel [${reel.id}] by user [${user.username}]"
        new UserReelVideoActivity(user: user, reel: reel, video: video, type: ActivityType.AddVideoToReel.value).save()
    }

    void videoRemovedFromReel(User user, Reel reel, Video video) {
        log.info "Video [${video.id}] has been removed from reel [${reel.id}] by user [${user.username}]"
        UserReelVideoActivity.findByReelAndVideoAndType(reel, video, ActivityType.AddVideoToReel.value)?.delete()
    }

    void deleteAllUserActivity(User user) {
        log.info "Deleting all activity for user [${user.username}]"
        UserActivity.findAllByUser(user)*.delete()
    }

    List<UserReelActivity> findActivities(List<User> users, List<Reel> reels, Integer pageNumber = null) {
        boolean noUsers = (users == null || users.empty)
        boolean noReels = (reels == null || reels.empty)

        if(noUsers && noReels) {
            return []
        }

        def offset = pageNumber ? (pageNumber - 1) * maxActivitiesPerPage : 0
        UserReelActivity.withCriteria {
            or {
                if(users && !users.empty) {
                    inList('user', users)
                }
                if(reels && !reels.empty) {
                    inList('reel', reels)
                }
            }
            order('dateCreated', 'desc')
            order('type', 'desc')

            firstResult(offset as int)
            maxResults(maxActivitiesPerPage as int)
        }
    }
}
