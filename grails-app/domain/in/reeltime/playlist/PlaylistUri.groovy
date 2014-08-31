package in.reeltime.playlist

import in.reeltime.video.Video

class PlaylistUri {

    PlaylistType type
    String uri

    static belongsTo = [video: Video]

    static constraints = {
        type nullable: false
        uri nullable: false, blank: false
        video nullable: false
    }
}
