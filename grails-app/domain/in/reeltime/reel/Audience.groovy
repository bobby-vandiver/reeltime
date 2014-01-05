package in.reeltime.reel

import in.reeltime.user.User

class Audience {

    static hasMany = [users: User]

    static constraints = {
    }
}
