package in.reeltime.playlist

import in.reeltime.video.Video

class PlaylistUri {

    String uri
    static belongsTo = [video: Video]

    static constraints = {
        uri nullable: false, blank: false
        video nullable: false
    }
}
