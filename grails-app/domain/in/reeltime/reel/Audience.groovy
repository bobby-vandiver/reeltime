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

    static int countByAudienceMember(User member) {
        def criteria = Audience.createCriteria()
        def result = criteria.list {
            members {
                idEq(member.id)
            }
            projections {
                count()
            }
        }
        return result[0]
    }

    static constraints = {
    }

    boolean hasMember(User user) {
        return members?.contains(user)
    }
}
