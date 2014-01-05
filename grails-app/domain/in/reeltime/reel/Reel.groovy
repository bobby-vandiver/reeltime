package in.reeltime.reel

import in.reeltime.user.User

class Reel {

    static hasOne = [audience: Audience]

    static constraints = {
        audience unique: true
    }
}
