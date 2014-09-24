package in.reeltime.reel

import in.reeltime.user.User

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
