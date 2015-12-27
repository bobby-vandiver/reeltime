package in.reeltime.playlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.video.Video

@ToString
@EqualsAndHashCode(includes = ['playlistUri.id', 'video.id'])
class PlaylistUriVideo implements Serializable {

    private static final long serialVersionUID = 1

    PlaylistUri playlistUri
    Video video

    static constraints = {
        playlistUri nullable: false
        video nullable: false
    }

    static mapping = {
        id composite: ['playlistUri', 'video']
        version false
    }
}
