package in.reeltime.reel

import in.reeltime.user.User
import in.reeltime.video.Video

class Reel {

    static final UNCATEGORIZED_REEL_NAME = 'Uncategorized'

    static final MINIMUM_NAME_LENGTH = 5
    static final MAXIMUM_NAME_LENGTH = 25

    String name

    static searchable = {
        only = ['name']
    }

    static belongsTo = [owner: User]
    static hasOne = [audience: Audience]

    static constraints = {
        name nullable: false, blank: false, size: MINIMUM_NAME_LENGTH..MAXIMUM_NAME_LENGTH
        owner nullable: false
        audience unique: true
    }
}
