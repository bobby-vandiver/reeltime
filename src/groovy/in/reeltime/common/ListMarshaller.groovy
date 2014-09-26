package in.reeltime.common

import in.reeltime.activity.UserReelActivity
import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video
import in.reeltime.activity.ActivityType

// TODO: Use marshallers plugin if this becomes more involved
class ListMarshaller {

    static List marshallReelList(Collection<Reel> reels) {
        reels.collect([]) { reel ->
            [reelId: reel.id, name: reel.name]
        }
    }

    static List marshallVideoList(Collection<Video> videos) {
        videos.collect([]) { video ->
            [videoId: video.id]
        }
    }

    static List marshallUsersList(Collection<User> users) {
        users.collect([]) { user ->
            [username: user.username]
        }
    }

    static List marshallUserReelActivityList(Collection<UserReelActivity> activities) {
        def list = []
        activities.each { activity ->

            def type = activity.type
            def user = activity.user
            def reel = activity.reel

            def map = [
                    type: type.toString(),
                    user: [username: user.username],
                    reel: [
                            reelId: reel.id,
                            name: reel.name
                    ]
            ]

            if(type == ActivityType.AddVideoToReel) {
                def video = activity.video as Video
                map += [
                    video: [
                            videoId: video.id,
                            title: video.title
                    ]
                ]
            }
            list << map
        }
        return list
    }
}
