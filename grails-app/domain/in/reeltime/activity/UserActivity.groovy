package in.reeltime.activity

import in.reeltime.user.User

abstract class UserActivity {

    User user
    Integer type

    Date dateCreated

    static constraints = {
        user nullable: false
        type nullable: false, inList: ActivityType.values()*.value
    }

    @Override
    public String toString() {
        return "UserActivity{" +
                "id=" + id +
                ", user=" + user +
                ", type=" + type +
                ", dateCreated=" + dateCreated +
                ", version=" + version +
                '}';
    }
}
