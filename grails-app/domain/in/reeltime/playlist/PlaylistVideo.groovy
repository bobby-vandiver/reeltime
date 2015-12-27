package in.reeltime.playlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.video.Video

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['playlist.id', 'video.id'])
class PlaylistVideo implements Serializable {

    private static final long serialVersionUID = 1

    Playlist playlist
    Video video

    static constraints = {
        playlist nullable: false
        video nullable: false
    }

    static mapping = {
        id composite: ['playlist', 'video']
        version false
    }
}
