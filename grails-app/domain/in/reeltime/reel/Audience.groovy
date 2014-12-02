package in.reeltime.reel

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.user.User

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['reel'])
class Audience {

    static belongsTo = [reel: Reel]
    static hasMany = [members: User]

    static List<Audience> findAllByAudienceMember(User member) {
        Audience.withCriteria {
            members {
                idEq(member.id)
            }
        }
    }

    static constraints = {
    }

    boolean hasMember(User user) {
        return members?.contains(user)
    }
}
