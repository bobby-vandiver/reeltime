package in.reeltime.user

import in.reeltime.exceptions.AuthorizationException

class UserFollowingService {

    def maxUsersPerPage

    UserFollowing startFollowingUser(User follower, User followee) {
        log.info "User [${follower.username}] is attempting to follow user [${followee.username}]"

        if(follower == followee) {
            def message = "Cannot add follower [${follower.username}] as a followee"
            throw new AuthorizationException(message)
        }

        if(UserFollowing.findByFollowerAndFollowee(follower, followee)) {
            def message = "User [${follower.username}] cannot follow user [${followee.username}] multiple times"
            throw new AuthorizationException(message)
        }

        log.info "User [${follower.username}] is now following user [${followee.username}]"
        new UserFollowing(follower: follower, followee: followee).save()
    }

    void stopFollowingUser(User follower, User followee) {
        log.info "User [${follower.username}] is attempting to no longer follow user [${followee.username}]"
        def following = UserFollowing.findByFollowerAndFollowee(follower, followee)

        if(!following) {
            def message = "[${follower.username}] is not following [${followee.username}]"
            throw new AuthorizationException(message)
        }

        log.info "User [${follower.username}] is no longer follow user [${followee.username}]"
        following.delete()
    }

    List<User> listAllFolloweesForFollower(User follower) {
        UserFollowing.findAllByFollower(follower)?.collect { it.followee }
    }

    List<User> listAllFollowersForFollowee(User followee) {
        UserFollowing.findAllByFollowee(followee)?.collect { it.follower }
    }

    List<User> listFolloweesForFollower(User follower, int page) {
        def followeeIds = UserFollowing.findAllFolloweeIdsByFollower(follower)
        User.findAllByIdInListInAlphabeticalOrderByPage(followeeIds, page, maxUsersPerPage)
    }

    List<User> listFollowersForFollowee(User followee, int page) {
        def followerIds = UserFollowing.findAllFollowerIdsByFollowee(followee)
        User.findAllByIdInListInAlphabeticalOrderByPage(followerIds, page, maxUsersPerPage)
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
