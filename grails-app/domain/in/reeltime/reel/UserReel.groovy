package in.reeltime.reel

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.user.User

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['owner.username', 'reel.id'])
class UserReel implements Serializable {

    User owner
    Reel reel

    static constraints = {
        owner nullable: false
        reel nullable: false
    }

    static mapping = {
        id composite: ['owner', 'reel']
        version false
    }
}
