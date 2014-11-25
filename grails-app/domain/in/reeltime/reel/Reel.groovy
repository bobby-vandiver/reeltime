package in.reeltime.reel

import in.reeltime.user.User
import in.reeltime.video.Video

class Reel {

    static final UNCATEGORIZED_REEL_NAME = 'Uncategorized'

    static final MINIMUM_NAME_LENGTH = 5
    static final MAXIMUM_NAME_LENGTH = 25

    String name
    Date dateCreated

    static belongsTo = [owner: User]
    static hasOne = [audience: Audience]

    static transients = ['numberOfVideos', 'numberOfAudienceMembers', 'uncategorizedReel']

    static constraints = {
        name nullable: false, blank: false, minSize: MINIMUM_NAME_LENGTH, maxSize: MAXIMUM_NAME_LENGTH
        owner nullable: false
        audience unique: true
    }

    int getNumberOfVideos() {
        ReelVideo.countByReel(this)
    }

    int getNumberOfAudienceMembers() {
        audience?.members?.size() ?: 0
    }

    boolean isUncategorizedReel() {
        name == UNCATEGORIZED_REEL_NAME
    }

    boolean containsVideo(Video video) {
        Reel.exists(this?.id) && Video.exists(video?.id) &&
                ReelVideo.findByReelAndVideo(this, video) != null

    }
}
