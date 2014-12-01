package in.reeltime.user

import groovy.transform.ToString

@ToString(includeNames = true)
class UserFollowing {

    User follower
    User followee

    static constraints = {
        follower nullable: false
        followee nullable: false, validator: { val, obj -> val != obj.follower }
    }
}
