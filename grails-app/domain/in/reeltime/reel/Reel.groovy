package in.reeltime.reel

import in.reeltime.user.User
import in.reeltime.video.Video

class Reel {

    static final UNCATEGORIZED_REEL_NAME = 'Uncategorized'

    String name

    static belongsTo = [owner: User]
    static hasOne = [audience: Audience]
    static hasMany = [videos: Video]

    static constraints = {
        name nullable: false, blank: false, size: 5..25
        owner nullable: false
        audience unique: true
        videos nullable: false
    }

    boolean containsVideo(Video video) {
        return videos.contains(video)
    }
}
