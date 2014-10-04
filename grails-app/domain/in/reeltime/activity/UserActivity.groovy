package in.reeltime.activity

import in.reeltime.user.User

abstract class UserActivity {

    User user
    ActivityType type

    Date dateCreated

    static constraints = {
        user nullable: false
        type nullable: false
    }
}
