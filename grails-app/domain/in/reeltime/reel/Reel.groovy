package in.reeltime.reel

import in.reeltime.user.User

class Reel {

    String name

    static belongsTo = [owner: User]
    static hasOne = [audience: Audience]

    static constraints = {
        name nullable: false, blank: false, size: 5..25
        owner nullable: false
        audience unique: true
    }
}
