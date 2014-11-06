package test.helper

import in.reeltime.user.User
import in.reeltime.user.UserFollowing

class UserFollowingHelper {

    def userFollowingService

    List<User> addFolloweesToFollower(User follower, int count) {
        def followees = []

        count.times { it ->
            def followee = UserFactory.createUser('followee' + it)
            followees << followee

            userFollowingService.startFollowingUser(follower, followee)

            assert UserFollowing.findByFollowerAndFollowee(follower, followee) != null
        }

        return followees
    }

    List<User> addFollowersToFollowee(User followee, int count) {
        def followers = []

        count.times { it ->
            def follower = UserFactory.createUser('follower' + it)
            followers << follower

            userFollowingService.startFollowingUser(follower, followee)

            assert UserFollowing.findByFollowerAndFollowee(follower, followee) != null
        }

        return followers
    }

}
