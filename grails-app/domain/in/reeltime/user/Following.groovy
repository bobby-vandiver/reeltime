package in.reeltime.user

class Following {

    User follower
    User followee

    static constraints = {
        follower nullable: false
        followee nullable: false, validator: { val, obj -> val != obj.follower }
    }
}
