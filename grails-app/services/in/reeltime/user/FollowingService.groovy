package in.reeltime.user

class FollowingService {

    Following startFollowingUser(User follower, User followee) {
        log.info "User [${follower.username}] is attempting to follow user [${followee.username}]"

        if(follower == followee) {
            def message = "Cannot add follower [${follower.username}] as a followee"
            throw new IllegalArgumentException(message)
        }

        new Following(follower: follower, followee: followee).save()
    }

    void stopFollowingUser(User follower, User followee) {
        log.info "User [${follower.username}] is attempting to no longer follow user [${followee.username}]"
        def following = Following.findByFollowerAndFollowee(follower, followee)

        if(!following) {
            def message = "[${follower.username}] is not following [${followee.username}]"
            throw new IllegalArgumentException(message)
        }

        following.delete()
    }

    void removeFollowerFromAllFollowings(User follower) {
        log.info "Removing follower [${follower.username}] from all followings"
        Following.findAllByFollower(follower)*.delete()
    }

    void removeFolloweeFromAllFollowings(User followee) {
        log.info "Removing followee [${followee.username}] from all followings"
        Following.findAllByFollowee(followee)*.delete()
    }
}
