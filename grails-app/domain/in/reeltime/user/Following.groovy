package in.reeltime.user

class Following {

    User follower

    static hasMany = [followees: User]

    static constraints = {
        follower nullable: false
        followees nullable: false, validator: { val, obj -> !val.contains(obj.follower)}
    }
}
