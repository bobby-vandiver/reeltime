package in.reeltime.user

class FollowingService {

    Following createFollowingForFollower(User follower) {
        log.info "Creating following for follower [${follower.username}]"
        new Following(follower: follower, followees: []).save()
    }

    void deleteFollowingForFollower(User follower) {
        log.info "Deleting following for follower [${follower.username}]"
        Following.findByFollower(follower).delete()
    }

    // TODO: Remove user from all followings in which the user is a followee

    void startFollowingUser(User follower, User followee) {
        log.info "User [${follower.username}] is attempting to follow user [${followee.username}]"

        if(follower == followee) {
            def message = "Cannot add follower [${follower.username}] as a followee"
            throw new IllegalArgumentException(message)
        }

        def following = Following.findByFollower(follower)
        following.addToFollowees(followee)
        following.save()
    }

    void stopFollowingUser(User follower, User followee) {
        log.info "User [${follower.username}] is attempting to no longer follow user [${followee.username}]"
        def following = Following.findByFollower(follower)

        if(!following.followees.contains(followee)) {
            def message = "[${follower.username}] is not following [${followee.username}]"
            throw new IllegalArgumentException(message)
        }

        following.removeFromFollowees(followee)
        following.save()
    }
}
