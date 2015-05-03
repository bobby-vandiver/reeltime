package in.reeltime.user

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['follower', 'followee'])
class UserFollowing {

    User follower
    User followee

    static constraints = {
        follower nullable: false
        followee nullable: false, validator: { val, obj -> val != obj.follower }
    }

    static List<Long> findAllFolloweeIdsByFollower(User follower) {
        UserFollowing.withCriteria {
            eq('follower', follower)
            projections {
                property('followee.id')
            }
        } as List<Long>
    }

    static List<Long> findAllFollowerIdsByFollowee(User followee) {
        UserFollowing.withCriteria {
            eq('followee', followee)
            projections {
                property('follower.id')
            }
        } as List<Long>
    }
}
