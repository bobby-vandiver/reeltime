package in.reeltime.following

import in.reeltime.following.UserFollowing
import in.reeltime.user.User

class FollowingService {

    UserFollowing startFollowingUser(User follower, User followee) {
        log.info "User [${follower.username}] is attempting to follow user [${followee.username}]"

        if(follower == followee) {
            def message = "Cannot add follower [${follower.username}] as a followee"
            throw new IllegalArgumentException(message)
        }

        new UserFollowing(follower: follower, followee: followee).save()
    }

    void stopFollowingUser(User follower, User followee) {
        log.info "User [${follower.username}] is attempting to no longer follow user [${followee.username}]"
        def following = UserFollowing.findByFollowerAndFollowee(follower, followee)

        if(!following) {
            def message = "[${follower.username}] is not following [${followee.username}]"
            throw new IllegalArgumentException(message)
        }

        following.delete()
    }

    List<User> listFolloweesForFollower(User follower) {
        UserFollowing.findAllByFollower(follower)?.collect { it.followee }
    }

    List<User> listFollowersForFollowee(User followee) {
        UserFollowing.findAllByFollowee(followee)?.collect { it.follower }
    }

    void removeFollowerFromAllFollowings(User follower) {
        log.info "Removing follower [${follower.username}] from all followings"
        UserFollowing.findAllByFollower(follower)*.delete()
    }

    void removeFolloweeFromAllFollowings(User followee) {
        log.info "Removing followee [${followee.username}] from all followings"
        UserFollowing.findAllByFollowee(followee)*.delete()
    }
}
