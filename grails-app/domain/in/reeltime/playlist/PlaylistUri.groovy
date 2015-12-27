package in.reeltime.playlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.video.Video

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['uri'])
class PlaylistUri {

    PlaylistType type
    String uri

    static transients = ['video']

    static constraints = {
        type nullable: false
        uri nullable: false, blank: false, unique: true
    }

    Video getVideo() {
        PlaylistUriVideo.findByPlaylistUri(this).video
    }
}
