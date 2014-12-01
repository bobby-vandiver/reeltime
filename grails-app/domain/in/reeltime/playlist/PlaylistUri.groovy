package in.reeltime.playlist

import groovy.transform.ToString
import in.reeltime.video.Video

@ToString(includeNames = true)
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
