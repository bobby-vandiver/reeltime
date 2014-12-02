package in.reeltime.activity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.user.User

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['user', 'type'])
abstract class UserActivity {

    User user
    Integer type

    Date dateCreated

    static constraints = {
        user nullable: false
        type nullable: false, inList: ActivityType.values()*.value
    }
}
