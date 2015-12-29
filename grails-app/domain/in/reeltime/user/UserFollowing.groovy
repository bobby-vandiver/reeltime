package in.reeltime.user

import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain

@ToString(includeNames = true)
class UserFollowing extends AbstractJoinDomain implements Serializable {

    private static final long serialVersionUID = 1

    User follower
    User followee

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        follower nullable: false
        followee nullable: false, validator: { val, obj -> val != obj.follower }
    }

    static mapping = {
        id composite: ['follower', 'followee']
        version false
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

    @Override
    String getLeftPropertyName() {
        return 'follower'
    }

    @Override
    String getRightPropertyName() {
        return 'followee'
    }
}
