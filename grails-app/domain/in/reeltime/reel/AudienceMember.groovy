package in.reeltime.reel

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.user.User

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['reel.id', 'video.id'])
class AudienceMember implements Serializable {

    private static final long serialVersionUID = 1

    Reel reel
    User member

    static constraints = {
        reel nullable: false
        member nullable: false
    }

    static mapping = {
        id composite: ['reel', 'member']
        version false
    }
}
